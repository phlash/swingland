package com.ashbysoft.swingland.event;

public abstract class AbstractEvent {
    private final Object _source;
    private final int _id;
    private boolean _consumed;
    protected AbstractEvent(Object source, int id) {
        _source = source;
        _id = id;
        _consumed = false;
    }
    public int getID() { return _id; }
    public Object getSource() { return _source; }
    public boolean isConsumed() { return _consumed; }
    public void consume() { _consumed = true; }
}
