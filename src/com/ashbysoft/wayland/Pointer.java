package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Pointer extends WaylandObject<Pointer.Listener> {
    public interface Listener {
        boolean pointerEnter(int serial, int surface, int x, int y);
        boolean pointerLeave(int serial, int surface);
        boolean pointerMove(int time, int x, int y);
        boolean pointerButton(int serial, int time, int button, int state);
        boolean pointerAxis(int time, int axis, int clicks);
        boolean pointerFrame();
    }
    public static final int RQ_SET_CURSOR = 0;
    public static final int RQ_RELEASE = 1;
    public static final int EV_ENTER = 0;
    public static final int EV_LEAVE = 1;
    public static final int EV_MOTION = 2;
    public static final int EV_BUTTON = 3;
    public static final int EV_AXIS = 4;
    public static final int EV_FRAME = 5;
    public static final int EV_AXIS_SOURCE = 6;
    public static final int EV_AXIS_STOP = 7;
    public static final int EV_AXIS_DISCRETE = 8;
    public static final int BUTTON_LEFT = 272;      // @see /usr/include/linux/input-event-codes.h:BTN_x
    public static final int BUTTON_RIGHT = 273;
    public static final int BUTTON_MIDDLE = 274;
    public static final int BUTTON_RELEASED = 0;
    public static final int BUTTON_PRESSED = 1;
    public static final int AXIS_VERTICAL = 0;
    public static final int AXIS_HORIZONTAL = 1;

    public Pointer(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_ENTER == op) {
            int serial = b.getInt();
            int surface = b.getInt();
            int x = b.getInt();
            int y = b.getInt();
            log(true, "enter:serial="+serial+" surface="+surface+" x="+(x>>8)+" y="+(y>>8));
            for (Listener l : listeners())
                if (!l.pointerEnter(serial, surface, x, y))
                    rv = false;
        } else if (EV_LEAVE == op) {
            int serial = b.getInt();
            int surface = b.getInt();
            log(true, "leave:serial="+serial+" surface="+surface);
            for (Listener l : listeners())
                if (!l.pointerLeave(serial, surface))
                    rv = false;
        } else if (EV_MOTION == op) {
            int time = b.getInt();
            int x = b.getInt();
            int y = b.getInt();
            log(true, "motion:time="+time+" x="+(x>>8)+" y="+(y>>8));
            for (Listener l : listeners())
                if (!l.pointerMove(time, x, y))
                    rv = false;
        } else if (EV_BUTTON == op) {
            int serial = b.getInt();
            int time = b.getInt();
            int button = b.getInt();
            int state = b.getInt();
            log(true, "button:serial="+serial+" time="+time+" button="+button+" state="+state);
            for (Listener l : listeners())
                if (!l.pointerButton(serial, time, button, state))
                    rv = false;
        } else if (EV_AXIS == op) {
            int time = b.getInt();
            int axis = b.getInt();
            int clicks = b.getInt();
            log(true, "axis:time="+time+",axis="+axis+",clicks="+clicks);
            for (Listener l : listeners())
                if (!l.pointerAxis(time, axis, clicks))
                    rv = false;
        } else if (EV_FRAME == op) {
            log(true, "frame");
            for (Listener l : listeners())
                if (!l.pointerFrame())
                    rv = false;
        } else if (op <= EV_AXIS_DISCRETE) {
            // unsupported at present - ignore quietly(ish)
            log(true, "unsupported:"+op);
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean setCursor(int serial, Surface surface, int x, int y) {
        ByteBuffer b = newBuffer(16, RQ_SET_CURSOR);
        b.putInt(serial);
        b.putInt(surface.getID());
        b.putInt(x);
        b.putInt(y);
        log(false, "setCursor:serial="+serial+" surface="+surface.getID()+" x="+x+" y="+y);
        return _display.write(b);
    }
    public boolean release() {
        ByteBuffer b = newBuffer(0, RQ_RELEASE);
        log(false, "release");
        return _display.write(b);
    }
}
