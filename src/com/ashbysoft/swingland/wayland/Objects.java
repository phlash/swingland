package com.ashbysoft.swingland.wayland;

import java.util.HashMap;

public class Objects {
    private static final Object _lock = new Object();
    private static int _lastID = Display.ID;
    private static HashMap<Integer, MessageReceiver> _objects = new HashMap<Integer, MessageReceiver>();
    // internal helper, call ONLY with _lock held!
    private static void register(MessageReceiver o, int id) {
        if (_objects.containsKey(id))
            throw new InternalError("Object map already contains ID: "+id);
        _objects.put(id, o);
    }
    // package-private display registrar
   static void registerDisplay(MessageReceiver o) {
        synchronized(_lock) {
            register(o, Display.ID);
        }
    }
    // standard object registrar
    public static int register(MessageReceiver o) {
        synchronized(_lock) {
            int id = ++_lastID;
            register(o, id);
            return id;
        }
    }
    public static void unregister(int id) {
        synchronized(_lock) {
            _objects.remove(id);
        }
    }
    public static MessageReceiver get(int id) {
        synchronized(_lock) {
            return _objects.get(id);
        }
    }
}