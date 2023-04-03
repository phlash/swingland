package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Callback extends WaylandObject<Callback.Listener> {
    public interface Listener {
        boolean done(int serial);
    }
    public static final int EV_DONE = 0;

    private boolean done = false;
    public Callback(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_DONE == op) {
            int serial = b.getInt();
            done = true;
            log(true, "done:serial="+serial);
            for (Listener l : listeners())
                if (!l.done(serial))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    public boolean done() { return done; }
}
