package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Registry extends WaylandObject<Registry.Listener> {
    public interface Listener {
        boolean global(int name, String iface, int version);
        boolean remove(int name);
    }
    public static final int RQ_BIND = 0;
    public static final int EV_GLOBAL = 0;
    public static final int EV_GLOBAL_REMOVE = 1;

    public Registry(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_GLOBAL == op) {
            int name = b.getInt();
            String iface = getString(b);
            int version = b.getInt();
            _log.info("global: name="+name+" iface="+iface+" version="+version);
            for (Listener l : listeners())
                if (!l.global(name, iface, version))
                    rv = false;
        } else if (EV_GLOBAL_REMOVE == op) {
            int name = b.getInt();
            _log.info("remove: name="+name);
            for (Listener l : listeners())
                if (!l.remove(name))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean bind(int name, String iface, int version, WaylandObject obj) {
        // [over]estimate buffer size from string length..
        ByteBuffer b = newBuffer(24+iface.length()*2, RQ_BIND);
        b.putInt(name);
        // https://www.mail-archive.com/wayland-devel@lists.freedesktop.org/msg40960.html
        putString(b, iface);
        b.putInt(version);
        b.putInt(obj.getID());
        // adjust buffer limit now we have actual size
        b.limit(b.position());
        return _display.write(b);
    }
}