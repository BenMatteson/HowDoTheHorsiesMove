package minichess;

import java.awt.*;
import java.util.Random;

/**
 * Created by ben on 4/15/2017.
 */
public class Move implements Comparable<Move>{
    //move from, to, and board for reference
    private Point src;
    private Point target;
    private Board board;
    //what was taken(blanks are '.' "pieces"
    private Piece took;
    //value of the move for move ordering
    private int value;
    private boolean promotion = false;

    public Move(Point piece, Point target, Board board) {
        this.src = piece;
        this.target = target;
        this.board = board;
        value = board.getSquare(target).getValue();
    }

    public Move(String move, Board board) {
        String[] pts = move.split("-");
        src = new Point((pts[0].charAt(0) - 'a'),6- Integer.parseInt(pts[0].substring(1)));
        target = new Point((pts[1].charAt(0) - 'a'),6- Integer.parseInt(pts[1].substring(1)));
        this.board = board;
        value = board.getSquare(target).getValue();
    }

    Point getSrc() {
        return src;
    }

    public Point getTarget() {
        return target;
    }

    void setTook(Piece took) {
        this.took = took;
    }

    Piece getTook() {
        return took;
    }

    public int getValue() {
        return value;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public void setPromotion(boolean promotion) {
        this.promotion = promotion;
    }

    public void setValue(int val) {
        value = val;
    }

    public void make() {
        board.doMove(this);
    }

    public void undo() {
        board.undoMove(this);
    }

    @Override
    public int compareTo(Move o) {
        return o.getValue() - getValue();
    }

    @Override
    public boolean equals(Object obj) {
        try{
            return getValue() == ((Move)obj).getValue();
        }
        catch (Exception e) {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return ((char) ('a' + src.x)) + ((6 - src.y) + "-") + ((char)('a' + target.x)) + (6 - target.y);
    }
}
