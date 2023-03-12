package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Callback extends WaylandObject {
    public static final int EV_DONE = 0;

    private boolean done = false;
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        if (EV_DONE == op) {
            _log.info("done: serial="+b.getInt());
            done = true;
        } else {
            _log.error("Unknown message opcode: "+op);
            return false;
        }
        return true;
    }
    public boolean done() { return done; }
}
