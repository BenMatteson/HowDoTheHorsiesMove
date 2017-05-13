import minichess.*;

import java.util.*;

/**
 * Created by ben on 5/8/2017.
 */
public class NegMaxPlayer extends Player{
    private int depth;
    private boolean ab;

    public NegMaxPlayer(Board board, boolean isWhite, boolean alphaBeta, int depth) {
        super(board, isWhite);
        this.depth = depth;
        ab = alphaBeta;
    }

    public NegMaxPlayer(Board board, boolean isWhite, boolean alphaBeta) {
        super(board, isWhite);
        ab = alphaBeta;
        if (alphaBeta)
            depth = 6;
        else
            depth = 6;
    }

    @Override
    public Move getPlay() {
        ArrayList<Move> moves = new ArrayList<>();
        PlayerPieces pieces;
        if(isWhite)
            pieces = board.whitePieces;
        else
            pieces = board.blackPieces;
        for (Piece p : pieces) {
            p.addMovesToList(moves);
        }
        if(!ab)
            evaluateMoves(moves, depth);
        else
            abEvaluate(moves, depth, -10000000, 10000000);
        Collections.sort(moves);
        return moves.get(0);
    }

    private int evaluateMoves(List<Move> moves, int depth) {
        if(depth > 0) {
            int value = Integer.MIN_VALUE;
            PlayerPieces pieces;
            if (!board.isWhiteTurn()) //grab opposite pieces to get moves after move. this will be accurate then too
                pieces = board.whitePieces;
            else
                pieces = board.blackPieces;
            for (Move move : moves) {
                if(move.getValue() > 100000)
                    return 1000000 + depth; //return early if taking a king, we found a win, add depth to favor faster win
                move.make();
                List<Move> moves2 = new ArrayList<>();
                for (Piece p : pieces) {
                    p.addMovesToList(moves2);
                }
                int moveVal = -evaluateMoves(moves2, depth - 1);
                move.setValue(moveVal);
                move.undo();
                value = Math.max(moveVal, value);
            }
            return value;
        }
        else
            return board.getValue();
    }

    private int abEvaluate(List<Move> moves, int depth, int alpha, int beta) {
        if(depth <= 0)
            return board.getValue(); // called from a leaf, just use heuristic valuation of board

        PlayerPieces pieces;
        if (!board.isWhiteTurn()) //grab opposite pieces to get moves after move. this will be accurate then too
            pieces = board.whitePieces;
        else
            pieces = board.blackPieces;
        int value = Integer.MIN_VALUE;

        for (Move move : moves) {
            if(move.getValue() > 100000)
                return 1000000 + depth; //return early if taking a king, we found a win, add depth to favor faster wins
            //make the move to analyze the board that results
            move.make();
            //create list of possible moves available to opponent
            List<Move> moves2 = new ArrayList<>();
            for (Piece p : pieces) {
                p.addMovesToList(moves2);
            }
            //recur on the evaluator to value this move
            int moveVal = -abEvaluate(moves2, depth - 1, -beta, -alpha);
            move.setValue(moveVal);
            //if the move is better than the best in another branch, we assume we won't be allowed to reach this branch
            if(moveVal >= beta) {
                move.undo(); //undo the move before we return
                return moveVal; //prune this move, opponent shouldn't let us get here...
            }
            //set the value to the highest seen in this branch
            value = Math.max(moveVal, value);
            //set the alpha to the highest seen overall (that wasn't pruned by early return above)
            alpha = Math.max(alpha, moveVal);
            //undo the move now that we've evaluated it
            move.undo();
        }
        return value;
    }
}
