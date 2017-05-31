package minichess;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by ben on 5/1/2017.
 */
public class Piece {
    private Board board;
    private char self;
    private boolean isWhite = false;
    private boolean isBlack = false;
    private int value;
    private Point location;

    public Piece() {
        board = null;//only need these for pieces that move
        location = null;

        self = '.';
    }

    public Piece(Board board, Point location, char c) {
        this.board = board;
        this.location = location;
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

    public int getValue() {
        if (value == 0) {
            switch (Character.toLowerCase(self)) {
                case 'p':
                    value = 150;
                    break;
                case 'r':
                    value = 500;
                    break;
                case 'b':
                    value = 200;
                    break;
                case 'n':
                    value = 400;
                    break;
                case 'q':
                    value = 900;
                    break;
                case 'k':
                    value = 10000000;
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
        this.location = location;
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
        symScan(moves, -dy, dx, stopShort, capture, steps - 1);
    }

    private void scanMoves(List<Move> moves, int dx, int dy, boolean stopShort, int capture) {
        int x, y;
        x = location.x;
        y = location.y;
        int xbound = Board.WIDTH - 1;
        int ybound = Board.HEIGHT - 1;
        do{
            x+=dx;
            y+=dy;
            if(x > xbound || y > ybound || x < 0 || y < 0)
                break;
            Piece target = board.getSquare(x, y);
            if(target.toChar() != '.') {
                if (isWhite == target.isWhite || capture == 0) //same color or can't cap
                    break;
                stopShort = true; //can cap this piece, but we can't continue further
            }
            else if(capture == -1)
                break;
            moves.add(new Move(location, new Point(x, y), board));
        } while (!stopShort);
    }
}
