package com.ashbysoft.wayland;

import com.ashbysoft.swingland.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// base class for all interactive objects
public abstract class WaylandObject {
    protected Logger _log = new Logger("["+getClass().getSimpleName()+"@"+hashCode()+"]:");
    private final int id = Objects.register(this);
    public int getID() { return id; }
    public abstract boolean handle(int oid, int op, int sz, ByteBuffer b);
    // parser/encoder helpers
    public ByteBuffer newBuffer(int size) {
        ByteBuffer r = ByteBuffer.allocate(size);
        r.order(ByteOrder.nativeOrder());
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