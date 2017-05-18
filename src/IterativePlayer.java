import minichess.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by ben on 5/14/2017.
 */
public class IterativePlayer extends Player {
    long end;
    long turnTime;// = 7200000000L;//7500000000 is equal split of time
    PlayerThread running;

    public IterativePlayer(Board board, boolean isWhite) {
        super(board, isWhite);
        end = System.nanoTime() + 280000000000L;//4:40:00
    }

    @Override
    public Move getPlay() {
        turnTime = (end - System.nanoTime()) / (80 - board.getPly());//remaining time / number of moves to make
        if(running != null) { //stop previous thread if there is one
            running.interrupt();
        }
        //start thread to calculate move
        running = new PlayerThread(new Board(board.toString()), isWhite);
        running.start();
        //delay for the move time determined above
        long turnStart = System.nanoTime();
        while (System.nanoTime() - turnStart < turnTime) {
            try {Thread.sleep(1);}
            catch (InterruptedException e) {
                continue;
            }
        }
        //if the current step is probably almost finished, let it run about how long we expect, if this isn't enough we need to give up
        if(System.nanoTime() - running.itrStart < running.predictedNext * .3) {
            long extra = running.predictedNext / 3;
            try {
                Thread.sleep(extra / 1000000, ((int) (extra % 1000000)));
            } catch (InterruptedException e) {
            }
        }
        //read the results of the last complete iteration
        return running.getMove();
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

    public PlayerThread(Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
        running = true;
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
        for (int i = 1; i < 10; i++) {//10 is probably as high as I'd ever want
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
            //System.out.println(i);
        }
    }

    public Move getMove() {
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