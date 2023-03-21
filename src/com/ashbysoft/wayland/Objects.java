package com.ashbysoft.wayland;

import com.ashbysoft.logger.Logger;
import java.util.HashMap;
import java.util.LinkedList;

public class Objects {
    private static final Object _lock = new Object();
    private static final Logger _log = new Logger("[Objects]:");
    private static int _lastID = Display.ID;
    private static final LinkedList<Integer> _reuse = new LinkedList<Integer>();
    private static final HashMap<Integer, WaylandObject> _objects = new HashMap<Integer, WaylandObject>();
    // internal helpers, call ONLY with _lock held!
    private static int nextID() {
        // first check the reuse queue
        if (_reuse.size()>0)
            return _reuse.remove();
        // otherwise allocate a new ID
        return ++_lastID;
    }
    private static void register(WaylandObject o, int id) {
        if (_objects.containsKey(id))
            throw new InternalError("Object map already contains ID: "+id);
        _log.detail("add:"+id+"->"+o.getName());
        _objects.put(id, o);
    }
    // standard object registrar
    public static int register(WaylandObject o) {
        synchronized(_lock) {
            // custom behaviour for the singleton Display object
            int id = (o instanceof Display) ? Display.ID : nextID();
            register(o, id);
            return id;
        }
    }
    public static void unregister(WaylandObject o) { unregister(o.getID()); }
    public static void unregister(int id) {
        synchronized(_lock) {
            _objects.remove(id);
            // custom behaviour for the singleton Display object
            if (id != Display.ID) {
                _reuse.add(id);
                _log.detail("reuse:"+id);
            } else {
                _log.detail("remove:"+id);
            }
        }
    }
    public static void clear() {
        synchronized(_lock) {
            _log.detail("clear");
            _objects.clear();
            _reuse.clear();
            _lastID = Display.ID;
        }
    }
    public static WaylandObject get(int id) {
        synchronized(_lock) {
            return _objects.get(id);
        }
    }
}