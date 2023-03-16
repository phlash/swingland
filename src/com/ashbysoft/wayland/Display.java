package com.ashbysoft.wayland;

import com.ashbysoft.logger.Logger;

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

    private Connection _conn;
    private Callback _sync;
    public Display() {
        this(null);
    }
    public Display(String path) {
        // connect or die..
        _conn = new Connection(path);
    }
    public void close() {
        if (_conn != null) {
            _conn.close();
            _conn = null;
        }
    }
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
            Objects.unregister(dobj);
        } else {
            return unknownOpcode(op);
        }
        return true;
    }

    // display synchronization helper
    public boolean roundtrip() {
        _log.detail("enter:roundtrip");
        if (null == _sync)
            _sync = new Callback();
        else
            Objects.reRegister(_sync);
        sync(_sync);
        boolean rv = false;
        while (!rv && dispatchOne()) {
            // we wait for the EV_DONE, and for the callback to be deleted
            if (_sync.done() && Objects.get(_sync.getID())==null) {
                rv = true;
            }
        }
        _log.detail("exit:roundtrip=true");
        return rv;
    }

    // display message pump, call often!
    public boolean dispatch() {
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
    private boolean dispatchOne() {
        //read and dispatch one message (if any)
        _log.detail("dispatchOne:enter");
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
        _log.detail("dispatchOne:exit="+rv);
        return rv;
    }

    // convenience package-private writers
    boolean write(ByteBuffer b) {
        return _conn.write(b);
    }
    boolean writeFD(ByteBuffer b, int fd) {
        return _conn.writeFD(b, fd);
    }

    // requests
    private boolean sync(Callback cb) {
        cb.reset();
        ByteBuffer m = newBuffer(12, RQ_SYNC);
        m.putInt(cb.getID());
        log(false, "sync->"+cb.getID());
        return _conn.write(m);
    }

    public boolean getRegistry(Registry r) {
        ByteBuffer m = newBuffer(12, RQ_GET_REGISTRY);
        m.putInt(r.getID());
        log(false, "getRegistry->"+r.getID());
        return _conn.write(m);
    }
}