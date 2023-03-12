package com.ashbysoft.wayland;

import com.ashbysoft.swingland.Logger;

// base class for all interactive objects
public abstract class WaylandObject {
    protected Logger _log = new Logger("["+getClass().getSimpleName()+"@"+hashCode()+"]:");
    private final int id = Objects.register(this);
    public int getID() { return id; }
    public abstract boolean handle(WaylandMessage e);
}