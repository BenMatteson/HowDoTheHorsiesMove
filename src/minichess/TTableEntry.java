package minichess;

import java.io.Serializable;

public class TTableEntry implements Serializable {
    byte depth;
    byte flag;
    int value;
    int key;
    int size;

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getKey() {
        return key;
    }

}
