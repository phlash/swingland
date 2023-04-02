package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Buffer extends WaylandObject<Buffer.Listener> {
    public interface Listener {
        boolean release();
    }
    public static final int RQ_DESTROY = 0;
    public static final int EV_RELEASE = 0;

    private final ByteBuffer _buffer;
    private volatile boolean _busy;

    public Buffer(Display d, ByteBuffer b) { super(d); _buffer = b; _busy = false; }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_RELEASE == op) {
            log(true, "release");
            _busy = false;
            for (Listener l : listeners())
                if (!l.release())
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    void setBusy() { _busy = true; }
    public boolean isBusy() { return _busy; }

    public boolean destroy() {
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        log(false, "destroy");
        return _display.write(b);
    }

    public ByteBuffer get() { return _buffer; }
}