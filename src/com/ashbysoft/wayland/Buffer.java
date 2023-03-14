package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Buffer extends WaylandObject<Buffer.Listener> {
    public interface Listener {
        boolean release();
    }
    public static final int RQ_DESTROY = 0;
    public static final int EV_RELEASE = 0;

    private final ByteBuffer _buffer;

    public Buffer(Display d, ByteBuffer b) { super(d); _buffer = b; }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_RELEASE == op) {
            _log.info("release");
            for (Listener l : listeners())
                if (!l.release())
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean destroy() {
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        return _display.write(b);
    }

    public ByteBuffer get() { return _buffer; }
}