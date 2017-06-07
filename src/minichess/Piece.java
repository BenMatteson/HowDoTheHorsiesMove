package minichess;

import java.awt.*;
import java.util.List;

/**
 * Created by ben on 5/1/2017.
 */
public class Piece {

    public static final int PAWN_VALUE = 150;
    public static final int KING_VALUE = 10000000;//should be order(s) of magnitude > sum of all others
    public static final int QUEEN_VALUE = 900;//can have up to 6 if all pawns promote
    public static final int KNIGHT_VALUE = 350;
    public static final int ROOK_VALUE = 500;
    public static final int BISHOP_VALUE = 300;


    private Board board;
    private char self;
    private boolean isWhite = false;
    private boolean isBlack = false;
    private int value;
    private byte locx;
    private byte locy;

    public Piece() {
        board = null;//only need this for pieces that move

        self = '.';
    }

    public Piece(Board board, Point location, char c) {
        this(board, location.x, location.y, c);
    }

    public Piece(Board board, int xloc, int yloc, char c) {
        this.board = board;
        this.locx = (byte)xloc;
        locy = (byte)yloc;
        self = c;
        isWhite = Character.isUpperCase(c);
        isBlack = Character.isLowerCase(c);
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isBlack() {
        return isBlack;
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
        if (value == 0) {
            switch (Character.toLowerCase(self)) {
                case 'p':
                    value = PAWN_VALUE;
                    break;
                case 'r':
                    value = ROOK_VALUE;
                    break;
                case 'b':
                    value = BISHOP_VALUE;
                    break;
                case 'n':
                    value = KNIGHT_VALUE;
                    break;
                case 'q':
                    value = QUEEN_VALUE;
                    break;
                case 'k':
                    value = KING_VALUE;
                    break;
                default:
                    value = 0;
            }
        }
        return value;
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

    public void addMovesToList(List<Move> moves) {
        switch (self) {
            case 'p':
                symScan(moves, 1, 1, true, -1, 2);
                scanMoves(moves, 0, 1, true, 0);
                break;
            case 'P':
                symScan(moves, -1, -1, true, -1, 2);
                scanMoves(moves, 0, -1, true, 0);
                break;
            case 'r':
            case 'R':
                symScan(moves, 1, 0);
                break;
            case 'b':
            case 'B':
                symScan(moves, 1, 1);
                symScan(moves, 1, 0, true, 0);
                break;
            case 'n':
            case 'N':
                symScan(moves, 2, 1, true, 1);
                symScan(moves, 2, -1, true, 1);
                break;
            case 'q':
            case 'Q':
                symScan(moves, 1, 0);
                symScan(moves, 1, 1);
                break;
            case 'k':
            case 'K':
                symScan(moves, 0, 1, true, 1);
                symScan(moves, 1, 1, true, 1);
                break;
            default:
                return;
        }

    }

    private void symScan(List<Move> moves, int dx, int dy) { // continuous scan in all directions, for Q,B,R
        symScan(moves, dx, dy, false, 1, 4);
    }

    private void symScan(List<Move> moves, int dx, int dy, boolean stopShort, int capture) { // stopshort scan in all directions for K,N,B(change color)
        symScan(moves, dx, dy, stopShort, capture, 4);
    }

    private void symScan(List<Move> moves, int dx, int dy, boolean stopShort, int capture, int steps) { // also used at 2 steps for pawn attacks
        if(steps <= 0) return;
        scanMoves(moves, dx, dy, stopShort, capture);
        symScan(moves, -dy, dx, stopShort, capture, --steps);
    }

    private void scanMoves(List<Move> moves, int dx, int dy, boolean stopShort, int capture) {
        byte x, y;
        x = locx;
        y = locy;
        int xBound = Board.WIDTH - 1;
        int yBound = Board.HEIGHT - 1;
        do{
            x+=dx;
            y+=dy;
            if(x > xBound || y > yBound || x < 0 || y < 0)
                break;

            Piece target = board.getSquare(x, y);

            if(target.toChar() != '.') {
                if (isWhite == target.isWhite || capture == 0) //same color or can't cap
                    break;
                stopShort = true; //can cap this piece, but we can't continue further
            }
            else if(capture == -1)
                break;

            moves.add(new Move(locx, locy, x, y, board));
        } while (!stopShort);
    }
}
