package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class XdgWmBase extends WaylandObject<XdgWmBase.Listener> {
    public interface Listener {
        boolean ping(int serial);
    }
    public static final int RQ_DESTROY = 0;
    public static final int RQ_CREATE_POSITIONER = 1;
    public static final int RQ_GET_XDG_SURFACE = 2;
    public static final int RQ_PONG = 3;
    public static final int EV_PING = 0;

    public XdgWmBase(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_PING == op) {
            int serial = b.getInt();
            log(true, "ping:serial="+serial);
            for (Listener l : listeners())
                if (!l.ping(serial))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean destroy() {
        ByteBuffer b = newBuffer(0, RQ_DESTROY);
        log(false, "destroy");
        return _display.write(b);
    }
    public boolean createPositioner(Positioner p) {
        ByteBuffer b = newBuffer(4, RQ_CREATE_POSITIONER);
        b.putInt(p.getID());
        log(false, "createPositioner->"+p.getID());
        return _display.write(b);
    }
    public boolean getXdgSurface(XdgSurface x, Surface s) {
        ByteBuffer b = newBuffer(8, RQ_GET_XDG_SURFACE);
        b.putInt(x.getID());
        b.putInt(s.getID());
        log(false, "getXdgSurface->"+x.getID()+" s="+s.getID());
        return _display.write(b);
    }
    public boolean pong(int serial) {
        ByteBuffer b = newBuffer(4, RQ_PONG);
        b.putInt(serial);
        log(false, "pong:serial="+serial);
        return _display.write(b);
    }
}