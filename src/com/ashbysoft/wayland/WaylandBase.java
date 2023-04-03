package com.ashbysoft.wayland;

import com.ashbysoft.logger.Logger;
import java.nio.ByteBuffer;

// common base class that defines object identity and event handler method
public abstract class WaylandBase {
    // default object name (type ID)
    protected String _name = "["+getClass().getSimpleName()+"@"+hashCode()+"]";
    // default logger with name
    protected Logger _log = new Logger(_name+":");
    // protocol objectID
    private final int _id;
    // connected display (if required in subtype)
    protected Display _display;

    protected WaylandBase(int id) {
        _id = id;
    }
    protected WaylandBase(Display d) {
        _id = d.register(this);
        _display = d;
    }
    public int getID() { return _id; }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        return unknownOpcode(op);
    }
    // default invalid opcode handler
    protected boolean unknownOpcode(int op) {
        _log.error("Unknown opcode: "+op);
        return false;
    }
    public String getName() { return _name; }
}
