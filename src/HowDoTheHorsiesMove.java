import minichess.*;

import java.util.PriorityQueue;

/**
 * Created by ben on 4/27/2017.
 */
public class HowDoTheHorsiesMove {
    public static void main(String[] args) {
        String server = "imcs.svcs.cs.pdx.edu";
        String port = "3589";
        String user = "HowDoTheHorsiesMove";
        String pass;
        boolean local = false;
        boolean isWhite = true;
        int playerType = 0; //0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random
        int player2Type = 0;


        for (int i = 0; i < args.length; ++i) {
            String s = args[i].toLowerCase();
            if (s.charAt(0) == '-') {
                for (int j = 0; j < s.length(); ++j) {
                    char c = s.charAt(j);
                    switch (Character.toLowerCase(c)) {
                        case 's':
                            server = args[++i];
                            break;
                        case 'u':
                            user = args[++i];
                            break;
                        case 'p':
                            pass = args[++i];
                            break;
                        case '2':
                            local = true;
                            player2Type = Character.getNumericValue(++i);
                            break;
                        case 'w':
                            isWhite = true;
                            break;
                        case'b':
                            isWhite = false;
                            break;
                        case 't':
                            playerType = Character.getNumericValue(++i);
                        case 'a':
                            if(s.charAt(++j) == 'b')
                                playerType = 1;
                            else
                                --j;
                        default:
                            System.err.println("Invalid flag '" + c + "', will be ignored.");
                    }
                }
            }
        }
        Board board = new Board("0 W\nkqbnr\nppppp\n.....\n.....\nPPPPP\nRNBQK");

        Player white = getPlayerType(board, true, playerType);
        if(local) {
            Player black = getPlayerType(board, false, player2Type);
            System.out.println(board);
            for (int i = 0; i <= 80; i++) {
                Move move;
                if (i % 2 == 0)
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

                if (board.getValue(true) > 100000) {
                    System.out.println(white + " wins!");
                    break;
                }
                if (board.getValue(false) > 100000) {
                    System.out.println(black + " wins!");
                    break;
                }
            }
        }
    }

    private static Player getPlayerType(Board board, boolean isWhite, int type) {
        switch (type) {//0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random
            case 0:
                return new NegMaxPlayer(board, isWhite, true, 7);//TODO fix when there's an itterative player
            case 1:
                return new NegMaxPlayer(board, isWhite, true, 7);
            case 2:
                return new NegMaxPlayer(board, isWhite, false, 6);
            case 3:
                return new RandomPlayer(board, isWhite);
            default:
                return null;
        }
    }
}