import minichess.*;

import java.util.*;


/**
 * Created by ben on 5/14/2017.
 */

public class IterativePlayer extends Player {
    long remaining;
    long turnTime;
    PlayerThread running;
    boolean ready;

    public IterativePlayer(Board board, boolean isWhite) {
        super(board, isWhite);
        remaining = 295000000000L;//4:55
        ready = false;
    }

    @Override
    public synchronized Move getPlay() {
        long startTime = System.nanoTime();
        ready = false;
        turnTime = remaining / ((82 - board.getPly()) / 2);//remaining time / number of moves to make

        if(running != null)  //stop previous thread if there is one
            running.interrupt();

        //start thread to calculate move
        running = new PlayerThread(new Board(board.toString()), isWhite, this);
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
            Thread.sleep(turnTime / 1000000, ((int) (turnTime % 1000000)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if the current step is probably almost finished, let it run about how long we expect,
        // if this isn't enough we need to give up
        if(System.nanoTime() - running.itrStart > running.predictedNext * .7) {//if elapsed > 70% of predicted for iteration
            long extra = running.predictedNext / 3;//give an extra 33% of predicted

            //set a timer to give up
            ready = false;
            running.requestNotify = true;//this is a bit of a race, but if we have rally bad luck the timeout means it's fine

            //wait for either the timer to expire or the iteration to complete
            try {
                wait((extra / 1000000) + 1);//add 1 to ensure not 0 which is infinite wait.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //deduct time spent from our timer, we use less than 5 minutes to account for other overhead
        remaining -= System.nanoTime() - startTime;

        //read the results of the last complete iteration
        return running.getMove();
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
    ArrayList<Move> moves;
    long itrStart;
    long itrEnd;
    long predictedNext;
    IterativePlayer p;
    boolean requestNotify;
    boolean read;

    public PlayerThread(Board board, boolean isWhite, IterativePlayer player) {
        this.board = board;
        this.isWhite = isWhite;
        running = true;
        p = player;
        requestNotify = true;
        read = false;
    }

    @Override
    public void run() {
        int depth = 0;
        moves = new ArrayList<>(30);//30 is probably about optimal
        PlayerPieces pieces;
        if(isWhite)
            pieces = board.whitePieces;
        else
            pieces = board.blackPieces;
        for (Piece p : pieces) {
            p.addMovesToList(moves);
        }
        //Collections.sort(moves);//we skip this sort since we recycle the move list anyway, the deepening does it
        for (int i = 1; i < 80; i++) {
            itrStart = System.nanoTime();
            try {
                itrEvaluate(moves, i, -10000000, 10000000);
            } catch (InterruptedException e) {
                return;
            }
            itrEnd = System.nanoTime();
            predictedNext = (itrEnd - itrStart) * 17;//guess at a good branching factor. seems like it hits often, probably good?
            Collections.sort(moves);
            move = moves.get(0);
            if(requestNotify) {
                p.gotMove();
                requestNotify = false;
            }

            //System.out.println(i);
        }
        //we wait if we've exited the loop in case the player hasn't retrieved it yet.
        while(!read){
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public Move getMove() {
        read = true;
        return move;
    }

    private int itrEvaluate(List<Move> moves, int depth, int alpha, int beta) throws InterruptedException {
        if(Thread.interrupted())//done with this thread, throw it out!
            throw new InterruptedException();
        if(depth <= 0)
            return board.getValue(); // called from a leaf, just use heuristic valuation of board

        PlayerPieces pieces;
        if (!board.isWhiteTurn()) //grab opposite pieces to get moves after move. this will be accurate then too
            pieces = board.whitePieces;
        else
            pieces = board.blackPieces;
        int value = Integer.MIN_VALUE;
        /*
        for (Move move : moves) {
        /*/
        int s = moves.size();
        for (int i = 0; i < s; i++) {
            Move move = moves.get(i);
            //*/
            if(move.getValue() > 9000000)
                return 100000 * depth; //return early if taking a king, we found a win, add depth to favor faster wins
            //make the move to analyze the board that results
            move.make(board);
            //create list of possible moves available to opponent
            //30 is big enough >99.9% of the time, and not having to grow the array makes a big difference
            List<Move> moves2 = new ArrayList<>(30);
            for (Piece p : pieces) {
                p.addMovesToList(moves2);
            }
            //recur on the evaluator to value this move
            int moveVal = -itrEvaluate(moves2, depth - 1, -beta, -alpha);
            move.setValue(moveVal);
            //if the move is better than the best in another branch, we assume we won't be allowed to reach this branch
            if(moveVal >= beta) {
                move.undo(board); //undo the move before we return
                return moveVal; //prune this move, opponent shouldn't let us get here...
            }
            //set the value to the highest seen in this branch
            value = Math.max(moveVal, value);
            //set the alpha to the highest seen overall (that wasn't pruned by early return above)
            alpha = Math.max(alpha, moveVal);
            //undo the move now that we've evaluated it
            move.undo(board);
        }
        return value;
    }
}