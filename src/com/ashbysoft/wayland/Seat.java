package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Seat extends WaylandObject<Seat.Listener> {
    public interface Listener {
        public static final int POINTER = 1;
        public static final int KEYBOARD = 2;
        public static final int TOUCH = 4;
        boolean seatCapabilities(int caps);
        boolean seatName(String name);
    }
    public static final int RQ_GET_POINTER = 0;
    public static final int RQ_GET_KEYBOARD = 1;
    public static final int RQ_GET_TOUCH = 2;
    public static final int RQ_RELEASE = 4;
    public static final int EV_CAPABILITIES = 0;
    public static final int EV_NAME = 1;

    public Seat(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_CAPABILITIES == op) {
            int caps = b.getInt();
            log(true, "capabilities:"+caps);
            for (Listener l : listeners())
                if (!l.seatCapabilities(caps))
                    rv = false;
        } else if (EV_NAME == op) {
            String name = getString(b);
            log(true, "name:"+name);
            for (Listener l : listeners())
                if (!l.seatName(name))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean getPointer(Pointer p) {
        ByteBuffer b = newBuffer(12, RQ_GET_POINTER);
        b.putInt(p.getID());
        log(false, "getPointer:obj="+p.getID());
        return _display.write(b);
    }
    public boolean getKeyboard(Keyboard k) {
        ByteBuffer b = newBuffer(12, RQ_GET_KEYBOARD);
        b.putInt(k.getID());
        log(false, "getKeyboard:obj="+k.getID());
        return _display.write(b);
    }
    public boolean getTouch(Touch t) {
        ByteBuffer b = newBuffer(12, RQ_GET_TOUCH);
        b.putInt(t.getID());
        log(false, "getTouch:obj="+t.getID());
        return _display.write(b);
    }
    public boolean release() {
        ByteBuffer b = newBuffer(8, RQ_RELEASE);
        log(false, "release");
        return _display.write(b);
    }
}
