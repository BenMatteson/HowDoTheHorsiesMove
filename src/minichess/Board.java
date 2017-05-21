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
        return setSquare(c,new Point(x,y));
    }

    private Piece setSquare(char c, Point loc) {
        return setSquare(new Piece(this, loc, c), loc);
    }

    private Piece setSquare(Piece piece, Point loc) {
        Piece ret = board[loc.x][loc.y];
        board[loc.x][loc.y] = piece;
        piece.setLocation(loc);
        return ret;
    }

    void doMove(Move move) {
        Piece piece = setSquare('.', move.getSrc());//grab the piece off the board
        Point dest = move.getTarget();//cache this point 'cause we use it a lot
        //check for promotion
        if (dest.y == HEIGHT - 1 && piece.toChar() == 'p') {//black pawn promotion, bottom of board
            move.setPromotion(true);
            blackPieces.remove(piece);//remove pawn from pieces
            piece = new Piece(this, dest, 'q');
            blackPieces.add(piece);//add queen to pieces
        }
        else if (dest.y == 0 && piece.toChar() == 'P') {//white pawn promotion, top of board
            move.setPromotion(true);
            whitePieces.remove(piece);
            piece = new Piece(this, dest, 'Q');
            whitePieces.add(piece);
        }
        else { //not a promotion, just move the piece
            piece.setLocation(dest);
        }

        Piece took = setSquare(piece, dest);//set piece to new location and save the piece that was captured
        move.setTook(took);

        if(took.isWhite()) whitePieces.remove(took);//remove piece list if needed
        else if(took.isBlack()) blackPieces.remove(took);
        ++ply;
    }

    void undoMove(Move move) {
        //store values we need multiple times
        Point src = move.getSrc();
        Point target = move.getTarget();
        Piece piece = getSquare(target);//grab the piece from where it moved to

        //handle promotion
        if (move.isPromotion()) {
            if (piece.isWhite()) {//was white pawn
                whitePieces.remove(piece);
                setSquare('P', src);
                whitePieces.add(getSquare(src));
            }
            else {//must be black pawn
                blackPieces.remove(piece);
                setSquare('p', src);
                blackPieces.add(getSquare(src));
            }
        } else {//no promotion
            //moves the piece back
            piece.setLocation(src);
            setSquare(piece, src);
        }

        Piece took = move.getTook();
        if(took.isBlack())
            blackPieces.add(took);
        else if(took.isWhite())
            whitePieces.add(took);
        setSquare(took, target);
        --ply;
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
