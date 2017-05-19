import minichess.Board;
import minichess.Move;
import minichess.PlayerPieces;

import java.io.IOException;

/**
 * Created by ben on 5/14/2017.
 */
public class RemotePlayer extends Player {
    Client client;
    public RemotePlayer(Board board, boolean isWhite, Client client) {
        super(board, isWhite);
        this.client = client;
    }

    @Override
    public Move getPlay() {
        try {
            return new Move(client.getMove(), board);
        }
        catch (IOException io) {
            return getPlay();//try again?
        }
    }
}
