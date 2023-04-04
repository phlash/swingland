package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Positioner extends WaylandObject<Void> {
    public static final int RQ_DESTROY = 0;
    public static final int RQ_SET_SIZE = 1;
    public static final int RQ_SET_ANCHOR_RECT = 2;
    public static final int RQ_SET_ANCHOR = 3;
    public static final int RQ_SET_GRAVITY = 4;
    public static final int RQ_SET_CONADJ = 5;
    public static final int RQ_SET_OFFSET = 6;
    public static final int RQ_SET_REACTIVE = 7;
    public static final int RQ_SET_PARENT_SIZE = 8;
    public static final int RQ_SET_PARENT_CONFIG = 9;

    public static final int ANCHOR_NONE = 0;
    public static final int ANCHOR_TOP = 1;
    public static final int ANCHOR_BOTTOM = 2;
    public static final int ANCHOR_LEFT = 3;
    public static final int ANCHOR_RIGHT = 4;
    public static final int ANCHOR_TOP_LEFT = 5;
    public static final int ANCHOR_BOTTOM_LEFT = 6;
    public static final int ANCHOR_TOP_RIGHT = 7;
    public static final int ANCHOR_BOTTOM_RIGHT = 8;
    public static final int GRAVITY_NONE = ANCHOR_NONE;
    public static final int GRAVITY_TOP = ANCHOR_TOP;
    public static final int GRAVITY_BOTTOM = ANCHOR_BOTTOM;
    public static final int GRAVITY_LEFT = ANCHOR_LEFT;
    public static final int GRAVITY_RIGHT = ANCHOR_RIGHT;
    public static final int GRAVITY_TOP_LEFT = ANCHOR_TOP_LEFT;
    public static final int GRAVITY_BOTTOM_LEFT = ANCHOR_BOTTOM_LEFT;
    public static final int GRAVITY_TOP_RIGHT = ANCHOR_TOP_RIGHT;
    public static final int GRAVITY_BOTTOM_RIGHT = ANCHOR_BOTTOM_RIGHT;
    public static final int CONADJ_NONE = 0;
    public static final int CONADJ_SLIDE_X = 1;
    public static final int CONADJ_SLIDE_Y = 2;
    public static final int CONADJ_FLIP_X = 4;
    public static final int CONADJ_FLIP_Y = 8;
    public static final int CONADJ_RESIZE_X = 16;
    public static final int CONADJ_RESIZE_Y = 32;

    public Positioner(Display d) { super(d); }
    public boolean destroy() {
        log(false, "destroy");
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        return _display.write(b);
    }
    public boolean setSize(int w, int h) {
        log(false, "setSize:w="+w+",h="+h);
        ByteBuffer b = newBuffer(16, RQ_SET_SIZE);
        b.putInt(w);
        b.putInt(h);
        return _display.write(b);
    }
    public boolean setAnchorRect(int x, int y, int w, int h) {
        log(false, "setAnchorRect:x="+x+",y="+y+",w="+w+",h="+h);
        ByteBuffer b = newBuffer(24, RQ_SET_ANCHOR_RECT);
        b.putInt(x);
        b.putInt(y);
        b.putInt(w);
        b.putInt(h);
        return _display.write(b);
    }
    public boolean setAnchor(int a) {
        log(false, "setAnchor:a="+a);
        ByteBuffer b = newBuffer(12, RQ_SET_ANCHOR);
        b.putInt(a);
        return _display.write(b);
    }
    public boolean setGravity(int g) {
        log(false, "setGravity:g="+g);
        ByteBuffer b = newBuffer(12, RQ_SET_GRAVITY);
        b.putInt(g);
        return _display.write(b);
    }
    public boolean setConstraintAdjustment(int a) {
        log(false, "setConstraintAdjustment:a="+Integer.toHexString(a));
        ByteBuffer b = newBuffer(12, RQ_SET_CONADJ);
        b.putInt(a);
        return _display.write(b);
    }
    public boolean setOffset(int x, int y) {
        log(false, "setOffset:x="+x+",y="+y);
        ByteBuffer b = newBuffer(16, RQ_SET_OFFSET);
        b.putInt(x);
        b.putInt(y);
        return _display.write(b);
    }
    public boolean setReactive() {
        log(false, "setReactive");
        ByteBuffer b = newBuffer(8, RQ_SET_REACTIVE);
        return _display.write(b);
    }
    public boolean setParentSize(int w, int h) {
        log(false, "setParentSize:w="+w+",h="+h);
        ByteBuffer b = newBuffer(16, RQ_SET_PARENT_SIZE);
        b.putInt(w);
        b.putInt(h);
        return _display.write(b);
    }
    public boolean setParentConfig(int serial) {
        log(false, "setParentConfig:serial="+serial);
        ByteBuffer b = newBuffer(12, RQ_SET_PARENT_CONFIG);
        b.putInt(serial);
        return _display.write(b);
    }
}
