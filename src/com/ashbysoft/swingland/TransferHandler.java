// TODO..

package com.ashbysoft.swingland;

public class TransferHandler {
    public static final int NONE = 0;
    public static final int COPY = 1;
    public static final int MOVE = 2;
    public static final int COPY_OR_MOVE = 3;
    public static final int LINK = 0x40000000;

    protected TransferHandler() {}
    public TransferHandler(String property) {}
}