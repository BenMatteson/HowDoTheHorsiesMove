import minichess.*;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.security.spec.ECField;
import java.util.PriorityQueue;

/**
 * Created by ben on 4/27/2017.
 */
public class HowDoTheHorsiesMove {
    private static String server = "imcs.svcs.cs.pdx.edu";
    private static String port = "3589";
    private static String user = "HowDoTheHorsiesMove";
    private static String pass = "";
    private static boolean local = false;
    private static boolean isWhite = true;
    private static int playerType = 0; //0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random, 4 = server
    private static int player2Type = 4;
    private static boolean offer = true;
    private static String accept = "";
    private static int depth = 7;
    private static Client client = null;
    private static Board board = new Board("1 W\nkqbnr\nppppp\n.....\n.....\nPPPPP\nRNBQK");

    public static void main(String[] args) {

        //parse args, can use form "-xyz x_add z_add", or "-x x_add -y -z z_add" or any combination
        for (int i = 0; i < args.length; ++i) {
            String s = args[i].toLowerCase();
            if (s.charAt(0) == '-') {
                for (int j = 1; j < s.length(); ++j) {//start at 1 to skip '-'
                    char c = s.charAt(j);
                    switch (Character.toLowerCase(c)) {
                        case 's':
                            server = args[++i];//these increment first, so the next arg is read and will be skipped in loop
                            break;
                        case 'u':
                            user = args[++i];
                            break;
                        case 'p':
                            pass = args[++i];
                            break;
                        case '2':
                            local = true;
                            player2Type = Integer.parseInt(args[++i]);
                            break;
                        case 'w':
                            if(!isWhite) {
                                int temp = player2Type;
                                player2Type = playerType;
                                playerType = temp;
                            }
                            break;
                        case'b':
                            if(isWhite) {
                                int temp = player2Type;
                                player2Type = playerType;
                                playerType = temp;
                            }
                            break;
                        case 'a':
                            offer = false;
                            accept = args[++i];
                            break;
                        case 'o':
                            offer = true;
                            break;
                        case 't':
                            playerType = Integer.parseInt(args[++i]);
                            break;
                        case 'd':
                            depth = Integer.parseInt(args[++i]);
                            break;
                        default:
                            System.err.println("Invalid flag '" + c + "', will be ignored.");
                    }
                }
            }
        }

        //set up server connection unless playing local game
        if(!local) {
            if(pass == "") {
                System.out.println("must provide a password for networked game\nexiting");
                return;
            }
            try {
                client = new Client(server, port, user, pass);
                if(offer) {
                    isWhite = Character.toLowerCase(client.offer(isWhite ? 'w' : 'b')) == 'w'; //offer game, ensure correct player
                }
                else {
                    client.send("accept " + accept, false);
                    String r = client.expectResponse(false);
                    if(r == "105" && !isWhite) {//fix configuration if accepted as other color
                        isWhite = true;
                        int temp = player2Type;
                        player2Type = playerType;
                        playerType = temp;
                    } else if (r == "106" && isWhite) {
                        isWhite = false;
                        int temp = player2Type;
                        player2Type = playerType;
                        playerType = temp;
                    }
                }
            } catch (IOException io) {
                System.err.println("Error connecting to server, please check arguments and retry");
                System.err.println(io);
            }
        }

        //create players and play the game
        Player white = getPlayerType(board, true, playerType);
        Player black = getPlayerType(board, false, player2Type);
        if(local)System.out.println(board);

        for (int i = 1; i <= 80; i++) {
            Move move;
            if (i % 2 == 1)
                move = white.getPlay();
            else
                move = black.getPlay();
            if(local)System.out.println(move);
            if (move == null) {
                System.out.println("player ran out of moves");
                client.send("resign", true);
                try{client.close(); } catch (Exception e) { }
                return;
            }

            if(!local && board.isWhiteTurn() == isWhite)//networked game and is our turn, send move to server
                client.send(move.toString(), false);

            move.make();

            if(local) {
                System.out.println(board);
            }
                System.out.println(board.getValue());
            //}

            if (board.getValue(true) > 100000) {
                System.out.println(white + " wins!");
                break;
            }
            if (board.getValue(false) > 100000) {
                System.out.println(black + " wins!");
                break;
            }
        }
        try {client.close(); } catch (Exception e) { }
    }

    private static Player getPlayerType(Board board, boolean isWhite, int type) {
        switch (type) {//0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random
            case 0:
                return new NegMaxPlayer(board, isWhite, true, depth);//TODO fix when there's an itterative player
            case 1:
                return new NegMaxPlayer(board, isWhite, true, depth);
            case 2:
                return new NegMaxPlayer(board, isWhite, false, depth);
            case 3:
                return new RandomPlayer(board, isWhite);
            case 4:
                return new RemotePlayer(board, isWhite, client);
            default:
                return null;
        }
    }
}