package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class XdgPopup extends WaylandObject<XdgPopup.Listener> {
    public interface Listener {
        boolean xdgPopupConfigure(int x, int y, int w, int h);
        boolean xdgPopupDone();
        boolean xdgPopupRepositioned(int token);
    }
    public static final int RQ_DESTROY = 0;
    public static final int RQ_GRAB = 1;
    public static final int RQ_REPOSITION = 2;
    public static final int EV_CONFIGURE = 0;
    public static final int EV_DONE = 1;
    public static final int EV_REPOSITIONED = 2;

    public XdgPopup(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_CONFIGURE == op) {
            int x = b.getInt();
            int y = b.getInt();
            int w = b.getInt();
            int h = b.getInt();
            log(true, "configure:x="+x+",y="+y+",w="+w+",h="+h);
            for (Listener l : listeners())
                if (!l.xdgPopupConfigure(x, y, w, h))
                    rv = false;
        } else if (EV_DONE == op) {
            log(true, "done");
            for (Listener l : listeners())
                if (!l.xdgPopupDone())
                    rv = false;
        } else if (EV_REPOSITIONED == op) {
            int token = b.getInt();
            log(true, "repositioned:token="+token);
            for (Listener l : listeners())
                if (!l.xdgPopupRepositioned(token))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean destroy() {
        log(false, "destroy");
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        return _display.write(b);
    }
    public boolean grab(Seat seat, int serial) {
        log(false, "grab:seat="+seat.getID()+",serial="+serial);
        ByteBuffer b = newBuffer(16, RQ_GRAB);
        b.putInt(seat.getID());
        b.putInt(serial);
        return _display.write(b);
    }
    public boolean reposition(Positioner p, int token) {
        log(false, "reposition:pos="+p.getID()+",token="+token);
        ByteBuffer b = newBuffer(16, RQ_REPOSITION);
        b.putInt(p.getID());
        b.putInt(token);
        return _display.write(b);
    }
}
