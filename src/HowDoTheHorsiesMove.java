import minichess.*;

import java.io.IOException;

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
    private static int whiteDepth = 6;
    private static int blackDepth = -1;
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
                            port = args[++i];
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
                                switchColor();
                            }
                            break;
                        case'b':
                            if(isWhite) {
                                switchColor();
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
                            whiteDepth = Integer.parseInt(args[++i]);
                            break;
                        case 'f':
                            blackDepth = Integer.parseInt(args[++i]);
                            break;
                        default:
                            System.err.println("Invalid flag '" + c + "', will be ignored.");
                    }
                }
            }
        }
        if (blackDepth <= -1) blackDepth = whiteDepth; //allow using -d to set depth for either player in remote game
        //we allow setting 0 to run it as a pure heuristic player

        //set up server connection unless playing local game
        if(!local) {
            if(pass == "") {
                System.out.println("must provide a password for networked game\nexiting");
                return;
            }
            try {
                client = new Client(server,port);
                client.login(user,pass);
                if(offer) {
                    System.out.println("Playing as " + (isWhite ? "white" : "black"));
                    client.offerGameAndWait(isWhite ? 'W' : 'B'); //offer game, ensure correct player
                }
                else {
                    char r = Character.toUpperCase(client.accept(accept));
                    System.out.println("Playing as as " + (r == 'W' ? "white" : "black"));
                    if((r == 'W' && !isWhite) || (r == 'B' && isWhite)) {
                        switchColor();
                    }
                }
            } catch (IOException io) {
                System.err.println("Error connecting to server, please check arguments and retry");
                System.err.println(io);
            }
        }

        //create players and play the game
        Player white = getPlayerType(board, true, playerType, whiteDepth);
        Player black = getPlayerType(board, false, player2Type, blackDepth);
        System.out.println(board + "\n");

        //play they game
        for (int i = 1; i <= 80; i++) {
            Move move;
            if (i % 2 == 1)
                move = white.getPlay();
            else
                move = black.getPlay();
            if (move == null) {
                if(board.isWhiteTurn() == isWhite) {//our turn and can't move, resign.
                    System.out.println("player ran out of moves");
                } else {//not our turn, something went wrong, if it's not our fault we probably won
                    System.out.println("something went wrong, I should work on dealing with this better, but right now I just have to PANIC!");
                }
                if (!local) {
                    client.out.println("resign");
                    client.out.flush();

                    try {
                        client.close();
                    } catch (Exception e) {
                    }
                }
                //System.out.println("test");
                break;//ends game, tries to cleanup
            }

            //print move to standard out
            System.out.println(move);

            //if it's a networked game and is our turn, send our move to server
            if(!local && board.isWhiteTurn() == isWhite)
                try {
                client.sendMove(move.toString());
                } catch (Exception e){}

            //make the move on our board
            move.make(board);

            //print board state to standard out, as well as heuristic valuation of current state for player on move
            /*if(local)*/
            System.out.println(board);
            System.out.println(board.getValue() + "\n");

            //detect if it's a win, despite warnings, just using very large value for king to determine wins
            if (board.getValue(true) > 1000000) {
                System.out.println(white + " wins!");
                break;
            }
            if (board.getValue(false) > 1000000) {
                System.out.println(black + " wins!");
                break;
            }
        }
        System.out.println("Game Over");
        //do some cleanup
        if (!local) try {client.close(); } catch (Exception e) { }//close connection to server
        //terminate threads for iterative player
        try {
            ((IterativePlayer) white).terminate();
        }
        catch (Exception e) {}
        try {
            ((IterativePlayer) black).terminate();
        }
        catch (Exception e) {}
    }

    private static void switchColor() {
        int temp = player2Type;
        player2Type = playerType;
        playerType = temp;
        isWhite = !isWhite;
        System.out.println("switching color to " + (isWhite?'W':'B'));
    }

    private static Player getPlayerType(Board board, boolean isWhite, int type, int depth) {
        switch (type) {//0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random, 4 = server
            case 0:
                return new IterativePlayer(board, isWhite);//Iterative player
            case 1:
                return new NegMaxPlayer(board, isWhite, true, depth);//alpha-beta pruned player
            case 2:
                return new NegMaxPlayer(board, isWhite, false, depth);//negamax player
            case 3:
                return new RandomPlayer(board, isWhite);//as the name implies, random player
            case 4:
                return new RemotePlayer(board, isWhite, client);//wrapper for getting move from the server
            default:
                return null;
        }
    }
}