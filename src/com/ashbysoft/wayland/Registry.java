package com.ashbysoft.wayland;

import java.util.ArrayList;

import java.nio.ByteBuffer;

public class Registry extends WaylandObject {
    public interface Listener {
        boolean global(int name, String iface, int version);
        boolean remove(int name);
    }
    public static final int RQ_BIND = 0;
    public static final int EV_GLOBAL = 0;
    public static final int EV_GLOBAL_REMOVE = 1;

    private Display _display;
    private ArrayList<Listener> _listeners = new ArrayList<Listener>();

    public Registry(Display d) { _display = d; }
    public void addListener(Listener l) {
        _listeners.add(l);
    }
    public void removeListener(Listener l) {
        _listeners.remove(l);
    }

    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_GLOBAL == op) {
            int name = b.getInt();
            String iface = getString(b);
            int version = b.getInt();
            _log.info("global: name="+name+" iface="+iface+" version="+version);
            for (Listener l : _listeners)
                if (!l.global(name, iface, version))
                    rv = false;
        } else if (EV_GLOBAL_REMOVE == op) {
            int name = b.getInt();
            _log.info("remove: name="+name);
            for (Listener l : _listeners)
                if (!l.remove(name))
                    rv = false;
        } else {
            _log.error("Unknown message opcode: "+op);
            rv = false;
        }
        return rv;
    }

    public boolean bind(int name, WaylandObject obj) {
        ByteBuffer b = newBuffer(16);
        b.putInt(getID());
        b.putInt(RQ_BIND);
        b.putInt(name);
        b.putInt(obj.getID());
        return _display.write(b);
    }
}