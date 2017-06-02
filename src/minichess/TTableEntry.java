package minichess;

public class TTableEntry {
    int depth;
    int value;
    int flag;
    long key;

    public TTableEntry(long key, int depth, int value, int flag) {
        this.key = key;
        this.depth = depth;
        this.value = value;
        this.flag = flag;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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
        this.flag = flag;
    }

    public long getKey() {
        return key;
    }
}
