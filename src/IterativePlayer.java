import minichess.Board;
import minichess.Move;
import minichess.TTable;
import minichess.TTableEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Created by ben on 5/14/2017.
 */


public class IterativePlayer extends Player {
    long remaining;
    long turnTime;
    PlayerThread running;
    boolean ready;
    TTable table;

    //best guess at time/depth multiplier, used to wait out iterations that may be almost complete
    //the more accurate the better, but favoring low values will front load our time
    //front loading time is probably best because better moves early lead to stronger positions later.
    public static float TIME_MULTIPLIER = 3.9f;

    public IterativePlayer(Board board, boolean isWhite, int time) {
        super(board, isWhite);
        //(time in seconds)*(percent to use)*(remaining factor to convert to nanoseconds)
        remaining = time * 96 * 10000000L;
        ready = false;
        table = board.getTable();

        //start thinking immediately (mostly for if we're black)
        running = new PlayerThread(new Board(board.toString(), board.getTable()), isWhite, this);
        running.start();
    }

    @Override
    public synchronized Move getPlay() {
        long startTime = System.nanoTime();
        ready = false;
        turnTime = remaining / ((82 - board.getPly()) / 2);//remaining time / number of moves to make

        //stop previous thread
        running.interrupt();
        //start new thread to calculate move
        running = new PlayerThread(new Board(board.toString(), board.getTable()), isWhite, this);
        running.start();

        //first we wait for some value to be available at all
        while (!ready){
            try {
                wait(100);//this is not guaranteed to start before the other thread signaled ready, need short timeout
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //then delay for the move time determined above
        try {
            if(HowDoTheHorsiesMove.buildOpen) wait();//ignore; extra for easily building opening table
            wait(turnTime / 1000000, ((int) (turnTime % 1000000)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if the current step is probably almost finished, let it run about how long we expect,
        // if this isn't enough we need to give up
        if(System.nanoTime() - running.itrStart > running.predictedNext * .6) {//if elapsed > 60% of predicted for iteration
            long extra = running.predictedNext / 2250000;//44% of predicted as milliseconds. note: could be 0
            System.out.print(" Waiting:" + extra / 1000f + " ");//show wait as seconds
            running.requestNotify = true;//this is a bit of a race, but if we have rally bad luck the timeout means it's fine

            //wait for either the timer to expire or the iteration to complete
            try {
                wait(extra, 1);//add 1 nano to ensure not 0 which is infinite wait.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //read the results of the last complete iteration
        Move ret = running.getMove();

        //restart the thread to reflect that the active player has chosen a move.
        running.interrupt();
        running = new PlayerThread(new Board(board.toString(), board.getTable()), isWhite, this);
        running.start();

        //deduct time spent from our timer, we use less than 5 minutes total to account for other overhead
        //done immediately before return for most accurate value
        remaining -= System.nanoTime() - startTime;
        return ret;
    }

    synchronized void gotMove() {
        ready = true;
        notify();
    }

    public void terminate() {
        if(running != null)
            running.interrupt();
    }
}

class PlayerThread extends Thread {
    Board board;
    Move move;
    boolean isWhite;
    boolean running;
    List<Move> moves;
    long itrStart;
    long itrEnd;
    long predictedNext;
    IterativePlayer p;
    boolean requestNotify;
    boolean read;
    Random rnd;

    public PlayerThread(Board board, boolean isWhite, IterativePlayer player) {
        this.board = board;
        this.isWhite = isWhite;
        running = true;
        p = player;
        requestNotify = true;
        read = false;
        rnd = new Random();
    }

    @Override
    public void run() {
        moves = board.generateMoves();
        int maxDepth = HowDoTheHorsiesMove.MAX_PLY - board.getPly() + 1;
        for (int i = 1; i <= maxDepth; i++) {
            itrStart = System.nanoTime();
            try {
                itrEvaluate(moves, i, -10000000, 10000000);
            } catch (InterruptedException e) {
                return;
            }
            itrEnd = System.nanoTime();
            predictedNext = (long) ((itrEnd - itrStart) * IterativePlayer.TIME_MULTIPLIER);//best guess branching factor
            Collections.sort(moves);
            move = moves.get(0);
            if(requestNotify) {
                p.gotMove();
                requestNotify = false;
            }

            //extra junk to build an initialized ttable to speed up opening
            if(HowDoTheHorsiesMove.buildOpen) {
                try {
                    FileOutputStream fout = new FileOutputStream("TTable.ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(board.getTable());
                    oos.close();
                    fout.close();
                } catch (IOException e) {
                    System.err.println("error saving TTable");
                    e.printStackTrace();
                }
            }
            System.out.print(i + " ");
        }
        //we wait if we've exited the loop in case the player hasn't retrieved it yet.
        while(!read){
            try {
                p.gotMove();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public Move getMove() {
        read = true;
        return move;
    }

    private int itrEvaluate(List<Move> moves, int depth, int alpha, int beta) throws InterruptedException {
        if (Thread.interrupted())//done with this thread, throw it out!
            throw new InterruptedException();
        if (depth <= 0)
            return board.getValue(true); // called from a leaf, just use heuristic valuation of board

        int alphaOrig = alpha;
        TTable ttable = board.getTable();
        TTableEntry entry = null;
        boolean newEntry = false;

        //get info from ttable
        if (ttable != null) {
            entry = ttable.get(board.zobLow(), board.zobHigh());
            if (entry != null
                        && entry.getDepth() >= depth //entry is deep enough to use
                        && entry.getDepth() <= HowDoTheHorsiesMove.MAX_PLY - board.getPly()) { //depth not past end of game
                    if (entry.getFlag() == 0)//exact
                        return entry.getValue();
                    else if (entry.getFlag() < 0)//lower bound
                        alpha = Math.max(alpha, entry.getValue());
                    else if (entry.getFlag() > 0)//upper bound
                        beta = Math.min(beta, entry.getValue());
                    if (alpha >= beta)
                        return entry.getValue();
            } else {
                newEntry = true;
                entry = new TTableEntry(board.zobHigh(), depth);
            }
        }

        //do the search
        int bestValue = Integer.MIN_VALUE;
        int s = moves.size();
        //System.out.println(s);
        int deepSize = s;
        Collections.sort(moves);
        //*
        for (Move move : moves) { //seems to be the faster option
        /*/
        for (int i = 0; i < s; i++) { //rumored to be the faster option
            Move move = moves.get(i);
        //*/
            if(move.getValue() > 9000000) {
                bestValue = 100000 * depth; //return early if taking a king, we found a win, use depth to favor faster wins
                break;
            }
            //make the move to analyze the board that results
            move.make(board);

            //look deeper for moves that involve capturing pieces
            //tied to a flag for more granular board evaluation, otherwise it does bad things
            //but I don't know if I like the extra step to the search, so it's not always on
            //will decrement depth normally except when the last move is a capture otherwise
            int active = (Board.extra && depth <= 1 && move.wasCapture()) ? 0 : 1;//0 if a piece was captured by the last move

            //get list of possible moves available to opponent
            List<Move> moves2 = board.generateMoves();
            deepSize += moves2.size();//save the size of the top few tiers of current subtree for cheap size estimate
            //Collections.shuffle(moves2, rnd);

            //recur on the evaluator to value this move
            int moveVal = -itrEvaluate(moves2, depth - active, -beta, -alpha);
            moves2 = null;
            move.setValue(moveVal);
            //set the value to the highest seen in this branch
            bestValue = Math.max(moveVal, bestValue);
            //set the alpha to the highest seen overall (that wasn't pruned by early return above)
            alpha = Math.max(alpha, moveVal);
            //undo the move now that we've evaluated it
            move.undo(board);
            //if the move is better than the best in another branch, we assume we won't be allowed to reach this branch
            if(moveVal >= beta) {
                break;
            }
        }
        moves = null;

        //store our info in the ttable
        if(ttable != null) {
            entry.setSize(deepSize);
            entry.setValue(bestValue);
            if (bestValue <= alphaOrig)
                entry.setFlag(1);
            else if (bestValue >= beta)
                entry.setFlag(-1);
            else
                entry.setFlag(0);
            entry.setDepth(Math.max(depth, entry.getDepth()));
            //if(entry.getFlag() == 0)
                if (newEntry)
                    ttable.set(board.zobLow(), entry);
            //else
                //ttable.remove(board.zobLow());
        }

        return bestValue;
    }
}