package minichess;

import java.awt.*;
import java.util.List;

/**
 * Created by ben on 5/1/2017.
 */
public class Piece {

    public static final int PAWN_VALUE = 150;
    public static final int KING_VALUE = 10000000;//should be order(s) of magnitude > sum of all others
    public static final int QUEEN_VALUE = 600;//can have up to 6 if all pawns promote
    public static final int KNIGHT_VALUE = 350;
    public static final int ROOK_VALUE = 500;
    public static final int BISHOP_VALUE = 300;


    //private Board board;
    private char self;
    private byte locx;
    private byte locy;
    private char color = 0;

    public Piece() {
        //board = null;//only need this for pieces that move

        self = '.';
    }

    public Piece( Point location, char c) {
        this(location.x, location.y, c);
    }

    public Piece(int xloc, int yloc, char c) {
        //this.board = board;
        this.locx = (byte)xloc;
        locy = (byte)yloc;
        self = c;
        if(Character.isUpperCase(self))
            color = 'w';
        else if (Character.isLowerCase(self))
            color = 'b';
    }

    public boolean isWhite() {
        return color == 'w';
    }

    public boolean isBlack() {
        return color == 'b';
    }

    public static int getValue(char c) {
        switch (Character.toLowerCase(c)) {
            case 'p':
                return PAWN_VALUE;
            case 'r':
                return ROOK_VALUE;
            case 'b':
                return BISHOP_VALUE;
            case 'n':
                return KNIGHT_VALUE;
            case 'q':
                return QUEEN_VALUE;
            case 'k':
                return KING_VALUE;
            default:
                return 0;
        }
    }

    public int getValue() {
        return getValue(self);
    }

    public char toChar() {
        return self;
    }

    @Override
    public String toString() {
        return Character.toString(self);
    }

    public void setLocation(Point location) {
        this.locx = (byte)location.x;
        locy = (byte)location.y;
    }

    public void setLocation(int x, int y) {
        this.locx = (byte)x;
        locy = (byte)y;
    }

    public byte getY() {
        return locy;
    }

    public void addMovesToList(Board board, List<Move> moves) {
        switch (self) {
            case 'p':
                symScan(board, moves, 1, 1, true, -1, 2, this);
                scanMoves(board, moves, 0, 1, true, 0, this);
                break;
            case 'P':
                symScan(board, moves, -1, -1, true, -1, 2, this);
                scanMoves(board, moves, 0, -1, true, 0, this);
                break;
            case 'r':
            case 'R':
                symScan(board, moves, 1, 0, this);
                break;
            case 'b':
            case 'B':
                symScan(board, moves, 1, 1, this);
                symScan(board, moves, 1, 0, true, 0, this);
                break;
            case 'n':
            case 'N':
                symScan(board, moves, 2, 1, true, 1, this);
                symScan(board, moves, 2, -1, true, 1, this);
                break;
            case 'q':
            case 'Q':
                symScan(board, moves, 1, 0, this);
                symScan(board, moves, 1, 1, this);
                break;
            case 'k':
            case 'K':
                symScan(board, moves, 0, 1, true, 1, this);
                symScan(board, moves, 1, 1, true, 1, this);
                break;
            default:
                return;
        }

    }

    private static void symScan(Board board, List<Move> moves, int dx, int dy, Piece piece) { // continuous scan in all directions, for Q,B,R
        symScan(board, moves, dx, dy, false, 1, 4, piece);
    }

    private static void symScan(Board board, List<Move> moves, int dx, int dy, boolean stopShort, int capture, Piece piece) { // stopshort scan in all directions for K,N,B(change color)
        symScan(board, moves, dx, dy, stopShort, capture, 4, piece);
    }

    private static void symScan(Board board, List<Move> moves, int dx, int dy, boolean stopShort, int capture, int steps, Piece piece) { // also used at 2 steps for pawn attacks
        if(steps <= 0) return;
        scanMoves(board, moves, dx, dy, stopShort, capture, piece);
        symScan(board, moves, -dy, dx, stopShort, capture, --steps, piece);
    }

    private static void scanMoves(Board board, List<Move> moves, int dx, int dy, boolean stopShort, int capture, Piece piece) {
        byte x, y;
        x = piece.locx;
        y = piece.locy;
        int xBound = Board.WIDTH - 1;
        int yBound = Board.HEIGHT - 1;
        do{
            x+=dx;
            y+=dy;
            if(x > xBound || y > yBound || x < 0 || y < 0)
                break;

            Piece target = board.getSquare(x, y);

            if(target.toChar() != '.') {
                if (piece.isWhite() == target.isWhite() || capture == 0) //same color or can't cap
                    break;
                stopShort = true; //can cap this piece, but we can't continue further
            }
            else if(capture == -1)
                break;

            moves.add(new Move(piece.locx, piece.locy, x, y, board));
        } while (!stopShort);
    }
}
