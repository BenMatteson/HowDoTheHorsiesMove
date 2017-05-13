package minichess;

import java.util.LinkedList;

/**
 * Created by ben on 5/10/2017.
 */
public class PlayerPieces extends LinkedList<Piece> {
    int totalValue;

    public int getTotalValue() {
        return totalValue;
    }

    @Override
    public Piece removeFirst() {
        Piece ret = super.removeFirst();
        totalValue -= ret.getValue();
        return ret;
    }

    @Override
    public Piece removeLast() {
        Piece ret = super.removeLast();
        totalValue -= ret.getValue();
        return ret;
    }

    @Override
    public void addFirst(Piece piece) {
        super.addFirst(piece);
        totalValue += piece.getValue();
    }

    @Override
    public void addLast(Piece piece) {
        super.addLast(piece);
        totalValue += piece.getValue();
    }

    @Override
    public boolean add(Piece piece) {
        boolean ret = super.add(piece);
        if (ret) {
            totalValue += piece.getValue();
        }
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        if(ret) {
            totalValue -= ((Piece) o).getValue();
        }
        return ret;
    }

    @Override
    public Piece set(int index, Piece element) {
        Piece ret = super.set(index, element);
        totalValue -= ret.getValue();
        return ret;
    }

    @Override
    public void add(int index, Piece element) {
        super.add(index, element);
        totalValue += element.getValue();
    }

    @Override
    public Piece remove(int index) {
        Piece ret = super.remove(index);
        totalValue -= ret.getValue();
        return ret;
    }

    @Override
    public void push(Piece piece) {
        super.push(piece);
        totalValue += piece.getValue();
    }

    @Override
    public Piece pop() {
        Piece ret = super.pop();
        totalValue -= ret.getValue();
        return ret;
    }

    @Override
    public Piece remove() {
        Piece ret = super.remove();
        totalValue -= ret.getValue();
        return ret;
    }
}
