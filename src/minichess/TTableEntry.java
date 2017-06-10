package minichess;

import java.io.Serializable;

public class TTableEntry implements Serializable {
    byte depth;
    byte flag;
    int value;
    int key;
    float size;

    public TTableEntry(int key, int depth) {
        this.key = key;
        this.depth = ((byte)depth);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = ((byte)depth);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = ((byte)flag);
    }

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getKey() {
        return key;
    }

    public void age() {
        //we only use this size to weight values, so we can use it to age entries to keep the table fresh
        size -= .25f;// seems like a good age rate, may be on the high side though
    }

}
