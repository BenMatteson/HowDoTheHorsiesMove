package minichess;

import java.io.Serializable;

/**
 * Created by ben on 6/4/2017.
 */

//uses a two layer table to always store new value, and more useful of two older values on collision
public class TTable implements Serializable{
    private TTableEntry[] table;
    int capacity;
    int powOf2;

    public TTable(int powOf2) {
        this.powOf2 = powOf2;
        this.capacity = (int) Math.pow(2, powOf2) * 2;
        table = new TTableEntry[capacity * 2];
    }

    public TTableEntry get(int key, int conf) {
        int index = key >>> (32 - powOf2);
        if(table[index] != null && table[index].key == conf)
            return table[index];
        else if (table[index + capacity] != null && table[index].key == conf)
            return table[index + capacity];
        return null;
    }

    public void set(int key, TTableEntry entry) {
        int index = key >>> (32 - powOf2);
        TTableEntry first = table[index];
        if(first != null) {
            if (first.getSize() > entry.getSize()) {
                table[index + capacity] = entry;//replace second
                return;
            }
            else {
                table[index + capacity] = first;//move first to second, continued...
            }
        }
        table[index] = entry;//add at first
    }
}
