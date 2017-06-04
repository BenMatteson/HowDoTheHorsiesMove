import minichess.Board;
import minichess.Move;

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
            io.printStackTrace();//try again?
            return null;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
