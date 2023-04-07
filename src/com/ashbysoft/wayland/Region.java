package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Region extends WaylandObject<Void> {
    public static final int RQ_DESTROY = 0;
    public static final int RQ_ADD = 1;
    public static final int RQ_SUBTRACT = 2;

    public Region(Display d) { super(d); }
    public boolean destroy() {
        ByteBuffer b = newBuffer(0, RQ_DESTROY);
        log(false, "destroy");
        return _display.write(b);
    }
    public boolean add(int x, int y, int w, int h) {
        ByteBuffer b = newBuffer(16, RQ_ADD);
        b.putInt(x);
        b.putInt(y);
        b.putInt(w);
        b.putInt(h);
        log(false, "add:x="+x+" y="+y+" w="+w+" h="+h);
        return _display.write(b);
    }
    public boolean subtract(int x, int y, int w, int h) {
        ByteBuffer b = newBuffer(16, RQ_SUBTRACT);
        b.putInt(x);
        b.putInt(y);
        b.putInt(w);
        b.putInt(h);
        log(false, "subtract:x="+x+" y="+y+" w="+w+" h="+h);
        return _display.write(b);
    }
}