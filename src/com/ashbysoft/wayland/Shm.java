package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Shm extends WaylandObject<Shm.Listener> {
    public interface Listener {
        boolean format(int fid);
    }
    public static final int RQ_CREATE_POOL = 0;
    public static final int EV_FORMAT = 0;

    public Shm(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_FORMAT == op) {
            int fid = b.getInt();
            log(true, "format:0x"+Integer.toHexString(fid));
            for (Listener l : listeners())
                if (!l.format(fid))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public ShmPool createPool(int sz) {
        ShmPool p = new ShmPool(_display, sz);
        ByteBuffer b = newBuffer(8, RQ_CREATE_POOL);
        b.putInt(p.getID());
        b.putInt(sz);
        log(false, "createPool->"+p.getID());
        // ask ShmPool to add the file descriptor and write
        if (!p.writeFD(b)) {
            p = null;
        }
        return p;
    }
}