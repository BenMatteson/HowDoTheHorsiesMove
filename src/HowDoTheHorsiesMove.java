import minichess.*;

import java.io.IOException;
import java.security.spec.ECField;
import java.util.PriorityQueue;

/**
 * Created by ben on 4/27/2017.
 */
public class HowDoTheHorsiesMove {
    public static void main(String[] args) {
        String server = "imcs.svcs.cs.pdx.edu";
        String port = "3589";
        String user = "HowDoTheHorsiesMove";
        String pass = "";
        boolean local = false;
        boolean isWhite = true;
        int playerType = 0; //0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random, 4 = server
        int player2Type = 4;
        boolean offer = true;
        String accept = "";

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
                            int temp = player2Type;
                            player2Type = playerType;
                            playerType = temp;
                            break;
                        case 'a':
                            accept = args[++i];
                            break;
                        case 'o':
                            break;
                        case 't':
                            playerType = Character.getNumericValue(++i);
                        default:
                            System.err.println("Invalid flag '" + c + "', will be ignored.");
                    }
                }
            }
        }
        Board board = new Board("0 W\nkqbnr\nppppp\n.....\n.....\nPPPPP\nRNBQK");
        Client client = null;
        if(!local) {
            try {
                client = new Client(server, port, user, pass);
                if(offer) {
                    isWhite = Character.toLowerCase(client.offer(isWhite ? 'w' : 'b')) == 'w'; //offer game, ensure correct player
                    if (!client.expect("103", false)) {
                        System.out.println("error offering game, check args and try again");
                    }
                }
            } catch (IOException io) {
                System.err.println("Error connecting to server, please check arguments and retry");
                System.err.println(io);
            }
        }
        Player white = getPlayerType(board, true, playerType, client);
        Player black = getPlayerType(board, false, player2Type, client);
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
        try {
            client.close();
        } catch (Exception e) { }
    }

    private static Player getPlayerType(Board board, boolean isWhite, int type, Client client) {
        switch (type) {//0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random
            case 0:
                return new NegMaxPlayer(board, isWhite, true, 7);//TODO fix when there's an itterative player
            case 1:
                return new NegMaxPlayer(board, isWhite, true, 7);
            case 2:
                return new NegMaxPlayer(board, isWhite, false, 6);
            case 3:
                return new RandomPlayer(board, isWhite);
            case 4:
                return new RemotePlayer(board, isWhite, client);
            default:
                return null;
        }
    }
}