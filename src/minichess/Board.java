package minichess;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by ben on 4/15/2017.
 */
public class Board {
    private Piece[][] board;
    static final int HEIGHT = 6, WIDTH = 5;
    private int ply;
    //we cheat here and expose the piece lists because it's really expensive to copy them all the time otherwise...
    public PlayerPieces whitePieces;
    public PlayerPieces blackPieces;
    private long zobKey = 0;

    public Board() {
        board = new Piece[WIDTH][HEIGHT];
        ply = 0;
        blackPieces = new PlayerPieces();
        whitePieces = new PlayerPieces();
    }

    public Board(String board) {
        blackPieces = new PlayerPieces();
        whitePieces = new PlayerPieces();
        setBoard(board);
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getPly() {
        return ply;
    }

    public boolean isWhiteTurn() {
        return ply % 2 == 1;
    }

    //board value based purely on piece values
    //TODO make this more discerning
    public int getValue(boolean forWhite) {
        if(forWhite) {
            return whitePieces.getTotalValue() - blackPieces.getTotalValue();
        } else
            return blackPieces.getTotalValue() - whitePieces.getTotalValue();
    }

    // board value for active player
    public int getValue() {
        return getValue(isWhiteTurn());
    }

    public Piece getSquare(int x, int y) {
        return board[x][y];
    }

    public Piece getSquare(Point loc) {
        return getSquare(loc.x,loc.y);
    }

    private Piece setSquare(char c, int x, int y) {
        Piece ret = board[x][y];
        board[x][y] = new Piece(this, new Point(x,y), c);
        return ret;
    }

    private Piece setSquare(char c, Point loc) {
        return setSquare(c,loc.x,loc.y);
    }

    private Piece setSquare(Piece piece, Point loc) {
        Piece ret = board[loc.x][loc.y];
        board[loc.x][loc.y] = piece;
        piece.setLocation(loc);
        return ret;
    }

    boolean doMove(Move move) {
        Piece piece = getSquare(move.getSrc());
        piece.setLocation(move.getTarget());
        Piece took = setSquare(piece, move.getTarget());
        setSquare('.', move.getSrc());
        whitePieces.remove(took);
        blackPieces.remove(took);
        move.setTook(took);
        ++ply;
        return !(took == null);
    }

    boolean undoMove(Move move) {
        Point target = move.getTarget();
        Piece piece = getSquare(target);
        Point src = move.getSrc();
        //if (getSquare(src) == null) return false;
        //moves the src back
        piece.setLocation(src);
        setSquare(piece, src);
        Piece took = move.getTook();
        if(Character.isLowerCase(took.toChar()))
            blackPieces.add(took);
        else if(Character.isUpperCase(took.toChar()))
            whitePieces.add(took);
        setSquare(took, target);
        --ply;
        return true;
    }

    private void setBoard(String board) {

        String[] parts = board.split("\n");
        String[] hist = parts[0].split(" ");
        int play = new Integer(hist[0]);
        ply = 2 * play - (hist[1].toLowerCase().contains("w") ? 1 : 0);
        this.board = new Piece[WIDTH][HEIGHT];
        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                char c = parts[h+1].charAt(w);
                Piece p = new Piece(this, new Point(w,h), c);
                this.board[w][h] = p;
                if (Character.isUpperCase(c))
                    whitePieces.add(p);
                if (Character.isLowerCase(c))
                    blackPieces.add(p);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(((getPly()+1) / 2) + " " + (isWhiteTurn() ? "W" : "B") + "\n");
        for(int h = 0; h < HEIGHT; h++) {
            for(int w = 0; w < WIDTH; w++) {
                out.append(board[w][h]);
            }
            out.append('\n');
        }
        out.deleteCharAt(out.length()-1);
        return out.toString();
    }

    public long hash() {
        return zobKey;
    }

}
