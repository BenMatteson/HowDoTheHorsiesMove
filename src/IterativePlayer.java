import minichess.Board;
import minichess.Move;

import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;


/**
 * Created by ben on 5/14/2017.
 */
public class IterativePlayer extends Player {
    LocalTime start = LocalTime.now();
    LocalTime end = start.plusSeconds(300-5);
    LocalTime turnTime;

    public IterativePlayer(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public Move getPlay() {
        return null;
    }
}

class PlayerThread extends Thread {

}