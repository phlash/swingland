package com.ashbysoft.wayland;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class XdgToplevel extends WaylandObject<XdgToplevel.Listener> {
    public interface Listener {
        boolean configure(int w, int h, int[] states);
        boolean close();
    }
    public static final int RQ_DESTROY = 0;
    public static final int RQ_SET_PARENT = 1;
    public static final int RQ_SET_TITLE = 2;
    public static final int RQ_SET_APP_ID = 3;
    public static final int RQ_SHOW_WINDOW_MENU = 4;
    public static final int RQ_MOVE = 5;
    public static final int RQ_RESIZE_EDGE = 6;
    public static final int RQ_RESIZE = 7;
    public static final int RQ_SET_MAX_SIZE = 8;
    public static final int RQ_SET_MIN_SIZE = 9;
    public static final int RQ_SET_MAXIMIZED = 10;
    public static final int RQ_UNSET_MAXIMIZED = 11;
    public static final int RQ_SET_FULLSCREEN = 12;
    public static final int RQ_UNSET_FULLSCREEN = 13;
    public static final int RQ_SET_MINIMIZED = 14;
    public static final int EV_CONFIGURE = 0;
    public static final int EV_CLOSE = 1;

    public XdgToplevel(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_CONFIGURE == op) {
            int x = b.getInt();
            int y = b.getInt();
            int[] states = b.asIntBuffer().array();
            _log.info("configure: x="+x+" y="+y+" states="+Arrays.toString(states));
            for (Listener l : listeners())
                if (!l.configure(x, y, states))
                    rv = false;
        } else if (EV_CLOSE == op) {
            _log.info("close");
            for (Listener l : listeners())
                if (!l.close())
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    // XXX:TODO: UPTOHEREPHIL!
}