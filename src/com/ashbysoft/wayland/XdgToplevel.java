package com.ashbysoft.wayland;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class XdgToplevel extends WaylandObject<XdgToplevel.Listener> {
    public interface Listener {
        boolean xdgToplevelConfigure(int w, int h, int[] states);
        boolean xdgToplevelClose();
    }
    public static final int RQ_DESTROY = 0;
    public static final int RQ_SET_PARENT = 1;
    public static final int RQ_SET_TITLE = 2;
    public static final int RQ_SET_APP_ID = 3;
    public static final int RQ_SHOW_WINDOW_MENU = 4;
    public static final int RQ_MOVE = 5;
    public static final int RQ_RESIZE = 6;
    public static final int RQ_SET_MAX_SIZE = 7;
    public static final int RQ_SET_MIN_SIZE = 8;
    public static final int RQ_SET_MAXIMIZED = 9;
    public static final int RQ_UNSET_MAXIMIZED = 10;
    public static final int RQ_SET_FULLSCREEN = 11;
    public static final int RQ_UNSET_FULLSCREEN = 12;
    public static final int RQ_SET_MINIMIZED = 13;
    public static final int EV_CONFIGURE = 0;
    public static final int EV_CLOSE = 1;

    public XdgToplevel(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_CONFIGURE == op) {
            int w = b.getInt();
            int h = b.getInt();
            ByteBuffer ib = ByteBuffer.wrap(getArray(b));
            ib.order(ByteOrder.nativeOrder());
            int n = ib.limit()/4;
            int[] states = new int[n];
            for (int i=0; i<n; i++)
                states[i] = ib.getInt();
            log(true, "configure:w="+w+" h="+h+" states="+java.util.Arrays.toString(states));
            for (Listener l : listeners())
                if (!l.xdgToplevelConfigure(w, h, states))
                    rv = false;
        } else if (EV_CLOSE == op) {
            log(true, "close");
            for (Listener l : listeners())
                if (!l.xdgToplevelClose())
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    public boolean destroy() {
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        log(false, "destroy");
        return _display.write(b);
    }
    public boolean setParent(XdgToplevel parent) {
        ByteBuffer b = newBuffer(12, RQ_SET_PARENT);
        b.putInt(parent.getID());
        log(false, "setParent:"+parent.getID());
        return _display.write(b);
    }
    public boolean setTitle(String title) {
        ByteBuffer b = newBuffer(8+title.length()*2, RQ_SET_TITLE);
        putString(b, title);
        // adjust buffer limit now we know..
        b.limit(b.position());
        log(false, "setTitle:"+title);
        return _display.write(b);
    }
    public boolean setAppID(String app) {
        ByteBuffer b = newBuffer(8+app.length()*2, RQ_SET_APP_ID);
        putString(b, app);
        // adjust buffer limit now we know..
        b.limit(b.position());
        log(false, "setAppID:"+app);
        return _display.write(b);
    }
    public boolean showWindowMenu(Seat seat, int serial, int x, int y) {
        ByteBuffer b = newBuffer(24, RQ_SHOW_WINDOW_MENU);
        b.putInt(seat.getID());
        b.putInt(serial);
        b.putInt(x);
        b.putInt(y);
        log(false, "showWindowMenu:seat="+seat.getID()+" serial="+serial+" x="+x+" y="+y);
        return _display.write(b);
    }
    public boolean move(Seat seat, int serial) {
        ByteBuffer b = newBuffer(16, RQ_MOVE);
        b.putInt(seat.getID());
        b.putInt(serial);
        log(false, "move:seat="+seat.getID()+" serial="+serial);
        return _display.write(b);
    }
    public boolean resize(Seat seat, int serial, int edges) {
        ByteBuffer b = newBuffer(20, RQ_RESIZE);
        b.putInt(seat.getID());
        b.putInt(serial);
        b.putInt(edges);
        log(false, "resize:seat="+seat.getID()+" serial="+serial+" edges="+edges);
        return _display.write(b);
    }
    public boolean setMaxSize(int w, int h) {
        ByteBuffer b = newBuffer(16, RQ_SET_MAX_SIZE);
        b.putInt(w);
        b.putInt(h);
        log(false, "setMaxSize:w="+w+" h="+h);
        return _display.write(b);
    }
    public boolean setMinSize(int w, int h) {
        ByteBuffer b = newBuffer(16, RQ_SET_MIN_SIZE);
        b.putInt(w);
        b.putInt(h);
        log(false, "setMinSize:w="+w+" h="+h);
        return _display.write(b);
    }
    public boolean setMaximized() {
        ByteBuffer b = newBuffer(8, RQ_SET_MAXIMIZED);
        log(false, "setMaximized");
        return _display.write(b);
    }
    public boolean unsetMaximized() {
        ByteBuffer b = newBuffer(8, RQ_UNSET_MAXIMIZED);
        log(false, "unsetMaximized");
        return _display.write(b);
    }
    public boolean setFullscreen(Output o) {
        ByteBuffer b = newBuffer(12, RQ_SET_FULLSCREEN);
        b.putInt(o.getID());
        log(false, "setFullscreen:output="+o.getID());
        return _display.write(b);
    }
    public boolean unsetFullscreen() {
        ByteBuffer b = newBuffer(8, RQ_UNSET_FULLSCREEN);
        log(false, "unsetFullscreen");
        return _display.write(b);
    }
    public boolean setMinimized() {
        ByteBuffer b = newBuffer(8, RQ_SET_MINIMIZED);
        log(false, "setMinimized");
        return _display.write(b);
    }
}