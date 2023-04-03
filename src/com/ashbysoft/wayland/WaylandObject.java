package com.ashbysoft.wayland;

import com.ashbysoft.logger.Logger;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// base class for all protocol objects
public abstract class WaylandObject<T> extends WaylandBase {
    // listener support (if required in subtype)
    private ArrayList<T> _listeners;

    protected WaylandObject(int id) { super(id); }
    protected WaylandObject(Display d) {
        super(d);
    }
    // logging helper
    protected void log(boolean inOut, String msg) {
        _log.info("@"+getID()+(inOut ? "<IN>:" : "<OUT>:")+msg);
    }
    protected Iterable<T> listeners() {
        if (null == _listeners)
            return java.util.Collections.emptyList();
        return _listeners;
    }
    public void addListener(T l) {
        if (null == _listeners)
            _listeners = new ArrayList<T>();
        _listeners.add(l);
    }
    public void removeListener(T l) {
        if (null != _listeners)
            _listeners.remove(l);
    }
    // parser/encoder helpers
    public ByteBuffer newBuffer(int size, int request) {
        ByteBuffer r = ByteBuffer.allocate(size);
        r.order(ByteOrder.nativeOrder());
        // always add our ID and the request
        r.putInt(getID());
        r.putInt(request);
        return r;
    }
    public byte[] getArray(ByteBuffer b) {
        int l = b.getInt();
        int n = (l+3)/4*4;
        byte[] r = new byte[l];
        b.get(r);
        while (l<n) {
            b.get();
            l += 1;
        }
        return r;
    }
    public String getString(ByteBuffer b) {
        byte[] r = getArray(b);
        // clean up trailing NUL if present
        if (0 == r[r.length-1])
            return new String(r, 0, r.length-1);
        return new String(r);
    }
    public void putArray(ByteBuffer b, byte[] r) {
        int l = r.length;
        b.putInt(l);
        int n = (l+3)/4*4;
        b.put(r);
        while (l<n) {
            b.put((byte)0);
            l += 1;
        }
    }
    public void putString(ByteBuffer b, String s) {
        // we add a NUL terminator before encoding
        s = s+"\0";
        putArray(b, s.getBytes());
    }
}
