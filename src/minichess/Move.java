package minichess;

import java.awt.*;

/**
 * Created by ben on 4/15/2017.
 */
public class Move implements Comparable<Move>{
    //move from, to, and board for reference
    private byte srcx;
    private byte srcy;
    private byte targetx;
    private byte targety;
    //what was taken(blanks are piece objects too)
    private Piece took;
    //value of the move for move ordering
    private int value;
    private boolean promotion;

    public Move(Point piece, Point target) {
        this((byte)piece.x, (byte)piece.y, (byte)target.x, (byte)target.y);
    }

    public Move(byte piecex, byte piecey, byte targetx, byte targety) {
        this.srcx = piecex;
        this.srcy = piecey;
        this.targetx = targetx;
        this.targety = targety;
        value=0;// = board.getSquare(target).getValue();
        promotion = false;
    }

    public Move(String move, Board board) {
        if(move == null){
            throw new NullPointerException("null string in move constructor");
        }
        String[] pts = move.split("-");
        srcx = (byte)(pts[0].charAt(0) - 'a');
        srcy = (byte)(6- Integer.parseInt(pts[0].substring(1)));
        targetx = (byte)(pts[1].charAt(0) - 'a');
        targety = (byte)(6- Integer.parseInt(pts[1].substring(1)));
        value = board.getSquare(targetx,targety).getValue();
    }

    Point getSrc() {
        return new Point(srcx, srcy);
    }

    public Point getTarget() {
        return new Point(targetx, targety);
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

    public void make(Board board) {
        board.doMove(this);
    }

    public void undo(Board board) {
        board.undoMove(this);
    }

    public boolean wasCapture() {
        return took != null && took.toChar() != '.';
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
        return ((char) ('a' + srcx)) + ((6 - srcy) + "-") + ((char)('a' + targetx)) + (6 - targety);
    }
}
