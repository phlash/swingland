package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Compositor extends WaylandObject<Void> {
    public static final int RQ_CREATE_SURFACE = 0;
    public static final int RQ_CREATE_REGION = 1;

    public Compositor(Display d) { super(d); }

    public boolean createSurface(Surface s) {
        ByteBuffer b = newBuffer(12, RQ_CREATE_SURFACE);
        b.putInt(s.getID());
        log(false, "createSurface->"+s.getID());
        return _display.write(b);
    }
    public boolean createRegion(Region r) {
        ByteBuffer b = newBuffer(12, RQ_CREATE_REGION);
        b.putInt(r.getID());
        log(false, "createRegion->"+r.getID());
        return _display.write(b);
    }
}
