import minichess.*;

/**
 * Created by ben on 5/6/2017.
 */
public abstract class Player {
    protected Board board;
    protected boolean isWhite;

    public Player(Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
    }

    public abstract Move getPlay();

    @Override
    public String toString() {
        return isWhite ? "W" : "B";
    }
}
