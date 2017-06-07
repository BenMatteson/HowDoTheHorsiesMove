package minichess;

import java.awt.*;
import java.util.*;
import java.util.List;

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
    private long[][][] zobKeys;
    private long whiteKey, blackKey;
    private long zobKey;
    private TTable table;

    public static boolean extra = true;


    public Board(String board, TTable table) {
        this.table = table;
        blackPieces = new PlayerPieces();
        whitePieces = new PlayerPieces();
        //TODO cache this
        zobKeys = new long[WIDTH][HEIGHT][12];
        Random rnd = new Random(480312325	);
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < 12; j++) {
                for (int k = 0; k < HEIGHT; k++) {
                    zobKeys[i][k][j] = rnd.nextLong();
                }
            }
        }
        whiteKey = rnd.nextLong();
        blackKey = rnd.nextLong();
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
        //check ttable if available
        if(table != null) {
            TTableEntry entry = table.get(zobLow(), zobHigh());
        if(entry != null)
                return entry.getValue();//use value if we already calculated it to any depth
        }

        int value = 0;

        if(extra) {
            //mobility
            value += generateMoves(forWhite).size() - generateMoves(!forWhite).size();
            //pawn structure
            if(forWhite) {
                for (Piece p : whitePieces) {
                    if (p.toChar() == 'P')
                        value += 6-p.getY()-2;
                }
            }
            else {
                for (Piece p : blackPieces) {
                    if (p.toChar() == 'p')
                        value += p.getY()-2;
                }
            }
        }

        if (forWhite) {
            return value + whitePieces.getTotalValue() - blackPieces.getTotalValue();
        } else
            return value + blackPieces.getTotalValue() - whitePieces.getTotalValue();
    }

    // board value for active player
    public int getValue() {
        return getValue(isWhiteTurn());
    }

    public Piece getSquare(int x, int y) {
        return board[x][y];
    }

    public Piece getSquare(Point loc) {
        return getSquare(loc.x, loc.y);
    }

    private Piece setSquare(char c, int x, int y) {
        return setSquare(c, new Point(x, y));
    }

    private Piece setSquare(char c, Point loc) {
        return setSquare(new Piece(this, loc, c), loc);
    }

    private Piece setSquare(Piece piece, Point loc) {
        zobKey ^= getKeyFromPoint(loc);

        Piece ret = board[loc.x][loc.y];
        board[loc.x][loc.y] = piece;
        piece.setLocation(loc);

        zobKey ^= getKeyFromPoint(loc);

        return ret;
    }

    void doMove(Move move) {
        Piece piece = setSquare('.', move.getSrc());//grab the piece off the board
        Point dest = move.getTarget();//cache this point 'cause we use it a lot
        //check for promotion
        if (dest.y == HEIGHT - 1//index of bottom of board
                && piece.toChar() == 'p') {//black pawn up for promotion
            move.setPromotion(true);
            blackPieces.remove(piece);//remove pawn from pieces
            piece = new Piece(this, dest, 'q');
            blackPieces.add(piece);//add queen to pieces
        } else if (dest.y == 0 //index of top of board
                && piece.toChar() == 'P') {//white pawn up for promotion
            move.setPromotion(true);
            whitePieces.remove(piece);
            piece = new Piece(this, dest, 'Q');
            whitePieces.add(piece);
        } else { //not a promotion, just move the piece
            piece.setLocation(dest);
        }

        Piece took = setSquare(piece, dest);//set piece to new location and save the piece that was captured
        move.setTook(took);

        if (took.isWhite()) whitePieces.remove(took);//remove piece list if needed
        else if (took.isBlack()) blackPieces.remove(took);
        ++ply;
        zobKey ^= whiteKey;
        zobKey ^= blackKey;

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
            } else {//must be black pawn
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
        if (took.isBlack())
            blackPieces.add(took);
        else if (took.isWhite())
            whitePieces.add(took);
        setSquare(took, target);
        --ply;
        zobKey ^= whiteKey;
        zobKey ^= blackKey;
    }

    private void setBoard(String board) {

        String[] parts = board.split("\n");
        String[] hist = parts[0].split(" ");
        int play = new Integer(hist[0]);
        ply = 2 * play - (hist[1].toLowerCase().contains("w") ? 1 : 0);
        zobKey ^= (ply % 2 == 0) ? whiteKey : blackKey;
        this.board = new Piece[WIDTH][HEIGHT];
        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                char c = parts[h + 1].charAt(w);
                Point loc = new Point(w, h);
                Piece p = new Piece(this, loc, c);
                this.board[w][h] = p;
                if (Character.isUpperCase(c))
                    whitePieces.add(p);
                if (Character.isLowerCase(c))
                    blackPieces.add(p);
                zobKey ^= getKeyFromPoint(loc);
            }
        }
    }

    public TTable getTable() {
        return table;
    }

    public ArrayList<Move> generateMoves() {
        return generateMoves(isWhiteTurn());
    }

    public ArrayList<Move> generateMoves(boolean forWhite) {
        //30 is big enough >99.9% of the time, and not having to grow the array makes a big difference
        ArrayList<Move> moves = new ArrayList<>(30);
        PlayerPieces pieces;
        if (forWhite)
            pieces = whitePieces;
        else
            pieces = blackPieces;
        for (Piece p : pieces) {
            p.addMovesToList(moves);
        }
        return moves;
    }

    private long getKeyFromPoint(Point p) {
        char c = getSquare(p).toChar();
        int i;
        switch (c) {
            case 'p':
                i=0;
                break;
            case 'P':
                i=1;
                break;
            case 'n':
                i=2;
                break;
            case 'N':
                i=3;
                break;
            case 'b':
                i=4;
                break;
            case 'B':
                i=5;
                break;
            case 'r':
                i=6;
                break;
            case 'R':
                i=7;
                break;
            case 'q':
                i=8;
                break;
            case 'Q':
                i=9;
                break;
            case 'k':
                i=10;
                break;
            case 'K':
                i=11;
                break;
            default:
                return 0;//not a piece we should be hashing, this wastes some cycles, but keeps accuracy
        }
        return zobKeys[p.x][p.y][i];
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(((getPly() + 1) / 2) + " " + (isWhiteTurn() ? "W" : "B") + "\n");
        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                out.append(board[w][h]);
            }
            out.append('\n');
        }
        out.deleteCharAt(out.length() - 1);
        return out.toString();
    }

    public int zobHigh() {
        return (int) (zobKey >>> 32);
    }
    public int zobLow() {
        return (int)zobKey;
    }

}




