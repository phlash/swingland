package com.ashbysoft.swingland.event;

public abstract class AbstractEvent {
    protected final Object _source;
    protected final int _id;
    protected AbstractEvent(Object source, int id) {
        _source = source;
        _id = id;
    }
    public int getID() { return _id; }
    public Object getSource() { return _source; }
}
