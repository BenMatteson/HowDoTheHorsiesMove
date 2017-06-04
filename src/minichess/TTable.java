package minichess;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ben on 5/31/2017.
 */
public class TTable extends LinkedHashMap<Integer, TTableEntry> {
    int capacity;

    public TTable(int capacity) {
        super(capacity -= 5, 1);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, TTableEntry> eldest) {
        return size() > capacity;
    }

}
