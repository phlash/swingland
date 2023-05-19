package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class SubSurface extends WaylandObject<Void> {
    public static final int RQ_DESTROY = 0;
    public static final int RQ_SET_POSITION = 1;
    public static final int RQ_PLACE_ABOVE = 2;
    public static final int RQ_PLACE_BELOW = 3;
    public static final int RQ_SET_SYNC = 4;
    public static final int RQ_SET_DESYNC = 5;

    public SubSurface(Display d) { super(d); }
    public boolean destroy() {
        log(false, "destroy");
        ByteBuffer b = newBuffer(0, RQ_DESTROY);
        return _display.write(b);
    }
    public boolean setPosition(int x, int y) {
        log(false, "setPosition:x="+x+" y="+y);
        ByteBuffer b = newBuffer(8, RQ_SET_POSITION);
        b.putInt(x);
        b.putInt(y);
        return _display.write(b);
    }
    public boolean placeAbove(Surface s) {
        log(false, "placeAbove:s="+s.getID());
        ByteBuffer b = newBuffer(4, RQ_PLACE_ABOVE);
        b.putInt(s.getID());
        return _display.write(b);
    }
    public boolean placeBelow(Surface s) {
        log(false, "placeBelow:s="+s.getID());
        ByteBuffer b = newBuffer(4, RQ_PLACE_BELOW);
        b.putInt(s.getID());
        return _display.write(b);
    }
    public boolean setSync() {
        log(false, "setSync");
        ByteBuffer b = newBuffer(0, RQ_SET_SYNC);
        return _display.write(b);
    }
    public boolean setDesync() {
        log(false, "setDesync");
        ByteBuffer b = newBuffer(0, RQ_SET_DESYNC);
        return _display.write(b);
    }
}
