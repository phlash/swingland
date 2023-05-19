package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class SubCompositor extends WaylandObject<Void> {
    public static final int RQ_DESTROY = 0;
    public static final int RQ_GET_SUBSURFACE = 1;

    public SubCompositor(Display d) { super(d); }
    public boolean destroy() {
        log(false, "destroy");
        ByteBuffer b = newBuffer(0, RQ_DESTROY);
        return _display.write(b);
    }
    public boolean getSubSurface(SubSurface sub, Surface s, Surface p) {
        log(false, "getSubSurface: sub="+sub.getID()+" s="+s.getID()+" p="+p.getID());
        ByteBuffer b = newBuffer(12, RQ_GET_SUBSURFACE);
        b.putInt(sub.getID());
        b.putInt(s.getID());
        b.putInt(p.getID());
        return _display.write(b);
    }
}
