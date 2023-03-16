package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Callback extends WaylandObject {
    public static final int EV_DONE = 0;

    private boolean done = false;
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        if (EV_DONE == op) {
            log(true, "done:serial="+b.getInt());
            done = true;
        } else {
            return unknownOpcode(op);
        }
        return true;
    }
    public boolean reset() { boolean old = done; done = false; return old; }
    public boolean done() { return done; }
}
