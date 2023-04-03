package com.ashbysoft.wayland;

import com.ashbysoft.logger.Logger;
import java.util.HashMap;
import java.util.LinkedList;

public class Objects {
    private final Object _lock = new Object();
    private final Logger _log = new Logger("[Objects]:");
    private int _lastID;
    private final LinkedList<Integer> _reuse = new LinkedList<Integer>();
    private final HashMap<Integer, WaylandBase> _objects = new HashMap<Integer, WaylandBase>();

    public Objects(Display display) {
        synchronized(_lock) {
            register(display, display.getID());
            _lastID = display.getID();
        }
    }
    // internal helpers, call ONLY with _lock held!
    private int nextID() {
        // first check the reuse queue
        if (_reuse.size()>0)
            return _reuse.remove();
        // otherwise allocate a new ID
        return ++_lastID;
    }
    private void register(WaylandBase o, int id) {
        if (_objects.containsKey(id))
            throw new InternalError("Object map already contains ID: "+id);
        _log.detail("add:"+id+"->"+o.getName());
        _objects.put(id, o);
    }
    // standard object registrar
    public int register(WaylandBase o) {
        synchronized(_lock) {
            // custom behaviour for the singleton Display object
            int id = nextID();
            register(o, id);
            return id;
        }
    }
    public void unregister(WaylandBase o) { unregister(o.getID()); }
    public void unregister(int id) {
        synchronized(_lock) {
            _objects.remove(id);
            _reuse.add(id);
        }
        _log.detail("reuse:"+id);
    }
    public WaylandBase get(int id) {
        synchronized(_lock) {
            return _objects.get(id);
        }
    }
}
