import minichess.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Created by ben on 5/6/2017.
 */
public class RandomPlayer extends Player{

    public RandomPlayer(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public Move getPlay() {
        List<Move> moves = new ArrayList<>();
        PlayerPieces pieces;
        if(isWhite)
             pieces = board.whitePieces;
        else
            pieces = board.blackPieces;
        for (Piece p :
                pieces) {
            p.addMovesToList(moves);
        }
        if(moves.size() <= 0)
            return null;
        Move[] mvArr = new Move[moves.size()];
        Random rnd = new Random();
        return moves.toArray(mvArr)[rnd.nextInt(moves.size())];
    }
}
