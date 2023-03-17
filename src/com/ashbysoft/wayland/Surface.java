package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Surface extends WaylandObject<Surface.Listener> {
    public interface Listener {
        boolean enter(int outputID);
        boolean leave(int outputID);
    }
    // V1
    public static final int RQ_DESTROY = 0;
    public static final int RQ_ATTACH = 1;
    public static final int RQ_DAMAGE = 2;
    public static final int RQ_FRAME = 3;
    public static final int RQ_SET_OPAQUE_REGION = 4;
    public static final int RQ_SET_INPUT_REGION = 5;
    public static final int RQ_COMMIT = 6;
    // V2
    public static final int RQ_SET_BUFFER_TRANSFORM = 7;
    public static final int RQ_SET_BUFFER_SCALE = 8;
    public static final int RQ_DAMAGE_BUFFER = 9;

    public static final int EV_ENTER = 0;
    public static final int EV_LEAVE = 1;

    public static final int E_INVALID_SCALE = 0;
    public static final int E_INVALID_TRANSFORM = 1;

    public Surface(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_ENTER == op) {
            int out = b.getInt();
            log(true, "enter:"+out);
            for (Listener l : listeners())
                if (!l.enter(out))
                    rv = false;
        } else if (EV_LEAVE == op) {
            int out = b.getInt();
            log(true, "leave:"+out);
            for (Listener l : listeners())
                if (!l.leave(out))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    public boolean destroy() {
        ByteBuffer b = newBuffer(8, RQ_DESTROY);
        log(false, "destroy");
        return _display.write(b);
    }
    public boolean attach(Buffer buf, int x, int y) {
        ByteBuffer b = newBuffer(20, RQ_ATTACH);
        b.putInt(buf!=null? buf.getID(): 0);
        b.putInt(x);
        b.putInt(y);
        log(false, "attach:"+buf.getID());
        return _display.write(b);
    }
    public boolean damage(int x, int y, int w, int h) {
        ByteBuffer b = newBuffer(24, RQ_DAMAGE);
        b.putInt(x);
        b.putInt(y);
        b.putInt(w);
        b.putInt(h);
        log(false, "damage:x="+x+" y="+y+" w="+w+" h="+h);
        return _display.write(b);
    }
    public boolean frame(Callback cb) {
        ByteBuffer b = newBuffer(12, RQ_FRAME);
        b.putInt(cb.getID());
        log(false, "frame->"+cb.getID());
        return _display.write(b);
    }
    public boolean setOpaqueRegion(Region r) {
        ByteBuffer b = newBuffer(12, RQ_SET_OPAQUE_REGION);
        b.putInt(r.getID());
        log(false, "setOpaqueRegion<-"+r.getID());
        return _display.write(b);
    }
    public boolean setInputRegion(Region r) {
        ByteBuffer b = newBuffer(12, RQ_SET_INPUT_REGION);
        b.putInt(r.getID());
        log(false, "setInputRegion<-"+r.getID());
        return _display.write(b);
    }
    public boolean commit() {
        ByteBuffer b = newBuffer(8, RQ_COMMIT);
        log(false, "commit");
        return _display.write(b);
    }
    public boolean setBufferTransform(int tr) {
        ByteBuffer b = newBuffer(12, RQ_SET_BUFFER_TRANSFORM);
        b.putInt(tr);
        log(false, "setBufferTransform:"+tr);
        return _display.write(b);
    }
    public boolean setBufferScale(int sc) {
        ByteBuffer b = newBuffer(12, RQ_SET_BUFFER_SCALE);
        b.putInt(sc);
        log(false, "setBufferScale:"+sc);
        return _display.write(b);
    }
    public boolean damageBuffer(int x, int y, int w, int h) {
        ByteBuffer b = newBuffer(24, RQ_DAMAGE_BUFFER);
        b.putInt(x);
        b.putInt(y);
        b.putInt(w);
        b.putInt(h);
        log(false, "damageBuffer:x="+x+" y="+y+" w="+w+" h="+h);
        return _display.write(b);
    }
}