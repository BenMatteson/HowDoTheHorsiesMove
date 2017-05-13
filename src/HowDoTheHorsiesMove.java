import minichess.*;

import java.util.PriorityQueue;

/**
 * Created by ben on 4/27/2017.
 */
public class HowDoTheHorsiesMove {
    public static void main(String[] args) {
        Board board = new Board("0 W\nkqbnr\nppppp\n.....\n.....\nPPPPP\nRNBQK");
        Player white = new NegMaxPlayer(board, true, true);
        Player black = new NegMaxPlayer(board, false, true);
        System.out.println(board);
        for (int i = 0; i <= 80; i++) {
            Move move;
            if(i%2==0)
                move = white.getPlay();
            else
                move = black.getPlay();
            System.out.println(move);
            if (move == null) {
                System.out.println("player ran out of moves");
                return;
            }
            move.make();

            System.out.println(board);
            System.out.println(board.getValue());

            if(board.getValue(true) > 100000) {
                System.out.println(white + " wins!");
                break;
            }
            if(board.getValue(false) > 100000) {
                System.out.println(black + " wins!");
                break;
            }
        }
    }
}