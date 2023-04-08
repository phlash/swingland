package com.ashbysoft.wayland;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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
        // https://wayland-book.com/protocol-design/wire-protocol.html#messages
        // The message header is two words.
        ByteBuffer r = ByteBuffer.allocate(8 + size);
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
            return new String(r, 0, r.length-1, StandardCharsets.UTF_8);
        return new String(r, StandardCharsets.UTF_8);
    }
    // Pads to 32 bit boundary 
    // https://wayland-book.com/protocol-design/wire-protocol.html
    // array: A blob of arbitrary data, prefixed with a 32-bit integer specifying its length (in bytes), 
    // then the verbatim contents of the array, padded to 32 bits with undefined data.
    public int arrlen(int bytearraylen) {
        return 4 + (bytearraylen+3)/4*4;
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
    // Array capacity required to store string in UTF-8 + null terminator
    // which is the format required by wayland
    // https://wayland-book.com/protocol-design/wire-protocol.html
    // https://github.com/phlash/swingland/issues/1
    public int strlen(String s) {
        return arrlen(s.getBytes(StandardCharsets.UTF_8).length + 1);
    }
    public void putString(ByteBuffer b, String s) {
        var strbytes = s.getBytes(StandardCharsets.UTF_8);
        var foobytes = new byte[strbytes.length + 1];
        System.arraycopy(strbytes, 0, foobytes, 0, strbytes.length);
        foobytes[strbytes.length] = (byte)0;
        putArray(b, foobytes);
    }
}
