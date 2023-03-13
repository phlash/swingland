package com.ashbysoft.wayland;

import com.ashbysoft.swingland.Logger;

import java.nio.ByteBuffer;

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

    private Connection _conn;
    public Display() {
        this(null);
    }
    public Display(String path) {
        // connect or die..
        _conn = new Connection(path);
    }

    // message receiver
    public boolean handle(int oid, int op, int sz, ByteBuffer b) {
        if (EV_ERROR == op) {
            int eobj    = b.getInt();
            int ecode   = b.getInt();
            String emsg = getString(b);
            _log.error("Server error: object="+eobj+" code="+ecode+" msg="+emsg);
            return false;
        } else if (EV_DELETE_ID == op) {
            int dobj = b.getInt();
            _log.info("Delete object: "+dobj);
            Objects.unregister(dobj);
        } else {
            _log.error("Unknown message opcode: "+op);
            return false;
        }
        return true;
    }

    // display synchronization helper
    public boolean roundtrip() {
        Callback cb = new Callback();
        _log.info("Roundtrip: "+cb.getID());
        sync(cb.getID());
        while (dispatchOne()) {
            // we wait for the EV_DONE, and for the callback to be deleted
            if (cb.done() && Objects.get(cb.getID())==null)
                return true;
        }
        return false;
    }

    // display message pump, call often!
    public boolean dispatch() {
        // read messages, dispatch to registered objects until none left
        _log.info("Dispatch()");
        boolean rv = true;
        while (_conn.available()) {
            if (!dispatchOne())
                rv = false;
        }
        return rv;
    }
    private boolean dispatchOne() {
        //read and dispatch one message (if any)
        boolean rv = true;
        ByteBuffer b = _conn.read();
        int oid = b.getInt();
        int szop = b.getInt();
        WaylandObject o = Objects.get(oid);
        if (o != null) {
            if (!o.handle(oid, szop & 0xffff, (szop >> 16) & 0xffff, b))
                rv = false;
        } else {
            _log.detail("Ignoring event for missing object: "+oid);
        }
        return rv;
    }

    // convenience package-private writer
    boolean write(ByteBuffer b) {
        return _conn.write(b);
    }

    // requests
    private boolean sync(int cb) {
        ByteBuffer m = newBuffer(12);
        m.putInt(ID);
        m.putInt(RQ_SYNC);
        m.putInt(cb);
        return _conn.write(m);
    }

    public boolean getRegistry(Registry r) {
        ByteBuffer m = newBuffer(12);
        m.putInt(ID);
        m.putInt(RQ_GET_REGISTRY);
        m.putInt(r.getID());
        return _conn.write(m);
    }
}