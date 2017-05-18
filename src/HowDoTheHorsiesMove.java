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
                    client.offerGameAndWait(isWhite ? 'w' : 'b'); //offer game, ensure correct player
                }
                else {
                    char r = Character.toUpperCase(client.accept(accept));
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
        System.out.println(board);

        //play they game
        for (int i = 1; i <= 80; i++) {
            Move move;
            if (i % 2 == 1)
                move = white.getPlay();
            else
                move = black.getPlay();
            if (move == null) {
                System.out.println("player ran out of moves");
                client.out.println("resign");
                client.out.flush();
                try{client.close(); } catch (Exception e) { }
                return;
            }

            //print move to standard out
            System.out.println(move);

            //if it's a networked game and is our turn, send our move to server
            if(!local && board.isWhiteTurn() == isWhite)
                try {
                client.sendMove(move.toString());
                } catch (Exception e){}

            //make the move on our board
            move.make();

            //print board state to standard out, as well as heuristic valuation of current state for player on move
            System.out.println(board);
            System.out.println(board.getValue());

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
        try {client.close(); } catch (Exception e) { }
    }

    private static void switchColor() {
        int temp = player2Type;
        player2Type = playerType;
        playerType = temp;
        isWhite = !isWhite;
    }

    private static Player getPlayerType(Board board, boolean isWhite, int type, int depth) {
        switch (type) {//0 = default iterative deepening, 1 = alpha-beta, 2 = negamax, 3 = random, 4 = server
            case 0:
                return new NegMaxPlayer(board, isWhite, true, depth);//TODO fix when there's an iterative player
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