package com.ashbysoft.wayland;

import java.util.HashMap;

public class Objects {
    private static final Object _lock = new Object();
    private static int _lastID = Display.ID;
    private static HashMap<Integer, WaylandObject> _objects = new HashMap<Integer, WaylandObject>();
    // internal helper, call ONLY with _lock held!
    private static void register(WaylandObject o, int id) {
        if (_objects.containsKey(id))
            throw new InternalError("Object map already contains ID: "+id);
        _objects.put(id, o);
    }
    // standard object registrar
    public static int register(WaylandObject o) {
        synchronized(_lock) {
            // custom behaviour for the singleton Display object
            int id = (o instanceof Display) ? Display.ID : ++_lastID;
            register(o, id);
            return id;
        }
    }
    public static void unregister(int id) {
        synchronized(_lock) {
            _objects.remove(id);
        }
    }
    public static WaylandObject get(int id) {
        synchronized(_lock) {
            return _objects.get(id);
        }
    }
}