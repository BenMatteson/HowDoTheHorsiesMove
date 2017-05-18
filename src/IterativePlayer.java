import minichess.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Created by ben on 5/14/2017.
 */
public class IterativePlayer extends Player {
    long end;
    long turnTime = 7499000000l;//7500000000 is equal split of time
    PlayerThread running;

    public IterativePlayer(Board board, boolean isWhite) {
        super(board, isWhite);
        end = LocalTime.now().toNanoOfDay();
    }

    @Override
    public Move getPlay() {
        end = LocalTime.now().toNanoOfDay() + turnTime;
        if(running != null) {
            running.interrupt();
        }
        running = new PlayerThread(new Board(board.toString()), isWhite);
        running.start();
        while (LocalTime.now().toNanoOfDay() < end) {
            try {Thread.sleep(1);}
            catch (InterruptedException e) {
                continue;
            }
        }
        return running.getMove();
    }
}

class PlayerThread extends Thread {
    Board board;
    Move move;
    boolean isWhite;
    boolean running;
    ArrayList<Move> moves;

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
        //TODO decide if I want this sort
        //Collections.sort(moves);//sort moves by heuristic value first to hopefully improve pruning
        for (int i = 1; i < 10; i++) {//10 is probably as high as I'd ever want
            itrEvaluate(moves, i, -10000000, 10000000);
            if(!Thread.interrupted()) {//if the thread is interrupted we'll start getting garbage out so we don't want to save it
                Collections.sort(moves);
                move = moves.get(0);
            }
        }
    }

    public Move getMove() {
        return move;
    }

    private int itrEvaluate(List<Move> moves, int depth, int alpha, int beta) {
        if(Thread.interrupted())
            return 0;//we're throwing all this away anyway
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
            if(move.getValue() > 100000)
                return 1000000 + depth; //return early if taking a king, we found a win, add depth to favor faster wins
            //make the move to analyze the board that results
            move.make(board);
            //create list of possible moves available to opponent
            List<Move> moves2 = new ArrayList<>(30); //30 is big enough >99.9% of the time,
            // and not having to grow the array makes a big difference
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