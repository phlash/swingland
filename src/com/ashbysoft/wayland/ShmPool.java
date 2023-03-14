package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class ShmPool extends WaylandObject {
    public static final int RQ_CREATE_BUFFER = 0;
    public static final int RQ_DESTROY = 1;
    public static final int RQ_RESIZE = 2;

    private final int _fd;
    private final ByteBuffer _pool;

    public ShmPool(Display d, int size) {
        super(d);
        int f = Native.createSHM("/wayland-java-pool", size);
        if (f < 0)
            throw new RuntimeException("Unable to create shared memory");
        ByteBuffer p = Native.mapSHM(f, size);
        if (null == p)
            throw new RuntimeException("Unable to map shared memory");
        _fd = f;
        _pool = p;
    }
    // package-private write helper, avoids exposing _fd
    boolean writeFD(ByteBuffer b) {
        return _display.writeFD(b, _fd);
    }

    public Buffer createBuffer(int off, int w, int h, int s, int f) {
        // first map a view of the underlying buffer
        _pool.position(off);
        ByteBuffer v = _pool.slice();
        v.limit(h * s);
        // now associate that view with a buffer object
        Buffer buf = new Buffer(_display, v);
        ByteBuffer b = newBuffer(32, RQ_CREATE_BUFFER);
        b.putInt(buf.getID());
        b.putInt(off);
        b.putInt(w);
        b.putInt(h);
        b.putInt(s);
        b.putInt(f);
        if(!_display.write(b)) {
            buf = null;
            v = null;
        }
        return buf;
    }
    public boolean destroy() {
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        boolean rv = _display.write(b);
        // now destroy the local resources
        Native.releaseSHM(_fd, _pool);
        return rv;
    }
    public boolean resize(int s) {
        ByteBuffer b = newBuffer(12, RQ_RESIZE);
        b.putInt(s);
        return _display.write(b);
        // XXX:TODO: How to resize the local memory pool?
    }
}