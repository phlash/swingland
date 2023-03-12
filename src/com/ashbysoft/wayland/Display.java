package com.ashbysoft.wayland;

import com.ashbysoft.swingland.Logger;

public class Display extends WaylandObject {
    // The well-known display objectID
    public static final int ID = 1;
    // Request opcodes
    public static final int RQ_SYNC = 0;
    public static final int RQ_GET_REGISTRY = 1;
    // Event opcodes
    public static final int EV_ERROR = 0;
    public static final int EV_DELETE_ID = 1;
    // Error codes
    public static final int E_INVALID_OBJ = 0;
    public static final int E_INVALID_METHOD = 1;
    public static final int E_NO_MEMORY = 2;
    public static final int E_IMPLEMENTATION = 3;

    private Connection _display;
    public Display() {
        this(null);
    }
    public Display(String path) {
        // connect or die..
        _display = new Connection(path);
        // push a sync round to check everything is ok
        if (!roundtrip())
            throw new RuntimeException("Unable to sync() with server");
    }

    // message receiver
    public boolean handle(WaylandMessage e) {
        switch (e.opcode()) {
        case EV_ERROR:
            _log.error("Server error: object="+e.param(0)+" code="+e.param(1));
            break;
        case EV_DELETE_ID:
            _log.info("Delete object: "+e.param(0));
            Objects.unregister(e.param(0));
            return true;
        default:
            _log.error("Unknown message opcode: "+e.opcode());
            break;
        }
        return false;
    }

    // display synchronization helper
    public boolean roundtrip() {
        Callback cb = new Callback();
        _log.info("Roundtrip: "+cb.getID());
        sync(cb.getID());
        while (dispatchOne()) {
            if (cb.done())
                return true;
        }
        return false;
    }

    // display message pump, call often!
    public boolean dispatch() {
        // read messages, dispatch to registered objects until none left
        _log.info("Dispatch()");
        boolean rv = true;
        while (_display.available()) {
            if (!dispatchOne())
                rv = false;
        }
        return rv;
    }
    private boolean dispatchOne() {
        //read and dispatch one message (if any)
        boolean rv = true;
        WaylandMessage e = _display.read();
        WaylandObject o = Objects.get(e.object());
        if (o != null) {
            if (!o.handle(e))
                rv = false;
        } else {
            _log.detail("Ignoring event for missing object: "+e.object());
        }
        return rv;
    }

    // requests
    private boolean sync(int cb) {
        WaylandMessage r = new WaylandMessage(ID, RQ_SYNC);
        r.add(cb);
        return _display.write(r);
    }

    public boolean getRegistry(int reg) {
        WaylandMessage r = new WaylandMessage(ID, RQ_GET_REGISTRY);
        r.add(reg);
        return _display.write(r);
    }
}