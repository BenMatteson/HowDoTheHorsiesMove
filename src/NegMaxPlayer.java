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

    @Override
    public Move getPlay() {
        List<Move> moves = board.generateMoves();
        if(!ab)
            evaluateMoves(moves, depth);
        else {
            Collections.sort(moves);//sort moves by heuristic value first to hopefully improve pruning
            abEvaluate(moves, depth, -10000000, 10000000);
        }
        Collections.sort(moves);
        return moves.get(0);
    }

    int evaluateMoves(List<Move> moves, int depth) {
        if(depth > 0 && board.getPly() <= 80) {
            int value = Integer.MIN_VALUE;
            for (Move move : moves) {
                if(move.getValue() > 9000000)
                    return 100000 * depth; //return early if taking a king, we found a win, use depth to favor faster win
                move.make(board);
                List<Move> moves2 = board.generateMoves();
                //System.out.println(moves.size());
                int moveVal = -evaluateMoves(moves2, depth - 1);
                move.setValue(moveVal);
                move.undo(board);
                value = Math.max(moveVal, value);
            }
            return value;
        }
        else
            return board.getValue(false);
    }

    private int abEvaluate(List<Move> moves, int depth, int alpha, int beta) {
        if(depth <= 0)
            return board.getValue(false); // called from a leaf, just use heuristic valuation of board

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
            List<Move> moves2 = board.generateMoves();
            //recur on the evaluator to value this move
            int moveVal = -abEvaluate(moves2, depth - 1, -beta, -alpha);
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
