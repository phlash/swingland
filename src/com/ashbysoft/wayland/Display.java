package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Display extends WaylandObject<Display.Listener> {
    public interface Listener {
        void error(int id, int code, String msg);
    }
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

    // synchronization lock for all network I/O
    private Object _lock;
    private Connection _conn;
    // object registry
    private Objects _objects;
    public Display() {
        this(null);
    }
    public Display(String path) {
        super(ID);
        // connect or die..
        _conn = new Connection(path);
        _lock = new Object();
        _objects = new Objects(this);
    }
    public void close() {
        _log.info("close");
        synchronized(_lock) {
            if (_conn != null) {
                _conn.close();
                _conn = null;
            }
        }
    }
    public int register(WaylandBase o) { return _objects.register(o); }
    // message receiver
    public boolean handle(int oid, int op, int sz, ByteBuffer b) {
        if (EV_ERROR == op) {
            int eobj    = b.getInt();
            int ecode   = b.getInt();
            String emsg = getString(b);
            _log.error("Server error: object="+eobj+" code="+ecode+" msg="+emsg);
            for (Listener l : listeners())
                l.error(eobj, ecode, emsg);
            return false;
        } else if (EV_DELETE_ID == op) {
            int dobj = b.getInt();
            log(true, "delete:"+dobj);
            _objects.unregister(dobj);
        } else {
            return unknownOpcode(op);
        }
        return true;
    }

    // display synchronization helper
    public boolean roundtrip() {
        synchronized(_lock) {
            _log.detail("enter:roundtrip");
            Callback cb = new Callback(this);
            sync(cb);
            boolean rv = false;
            while (!rv && dispatchOne()) {
                // we wait for the EV_DONE, and for the callback to be deleted
                if (cb.done() && _objects.get(cb.getID())==null) {
                    rv = true;
                }
            }
            _log.detail("exit:roundtrip=true");
            return rv;
        }
    }

    // display message pump, call often!
    public boolean dispatch() {
        synchronized(_lock) {
            // read messages, dispatch to registered objects until none left
            _log.detail("dispatch:enter");
            boolean rv = true;
            while (_conn.available()) {
                if (!dispatchOne())
                    rv = false;
            }
            _log.detail("dispatch:exit="+rv);
            return rv;
        }
    }
    private boolean dispatchOne() {
        //read and dispatch one message (if any)
        _log.detail("dispatchOne:enter");
        boolean rv = true;
        ByteBuffer b = _conn.read();
        int oid = b.getInt();
        int szop = b.getInt();
        WaylandBase o = _objects.get(oid);
        if (o != null) {
            if (!o.handle(oid, szop & 0xffff, (szop >> 16) & 0xffff, b))
                rv = false;
        } else {
            _log.detail("Ignoring event for missing object: "+oid);
        }
        _log.detail("dispatchOne:exit="+rv);
        return rv;
    }

    // convenience package-private writers
    boolean write(ByteBuffer b) {
        synchronized(_lock) {
            return _conn.write(b);
        }
    }
    boolean writeFD(ByteBuffer b, int fd) {
        synchronized(_lock) {
            return _conn.writeFD(b, fd);
        }
    }

    // requests
    private boolean sync(Callback cb) {
        ByteBuffer m = newBuffer(4, RQ_SYNC);
        m.putInt(cb.getID());
        log(false, "sync->"+cb.getID());
        return _conn.write(m);
    }

    public boolean getRegistry(Registry r) {
        ByteBuffer m = newBuffer(4, RQ_GET_REGISTRY);
        m.putInt(r.getID());
        log(false, "getRegistry->"+r.getID());
        return write(m);
    }
}