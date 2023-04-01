package com.ashbysoft.swingland.event;

public abstract class AbstractEvent {
    private final Object _source;
    private final int _id;
    private boolean _consumed;
    private boolean _canSynthesize;
    protected AbstractEvent(Object source, int id) {
        _source = source;
        _id = id;
        _consumed = false;
        _canSynthesize = true;
    }
    public int getID() { return _id; }
    public Object getSource() { return _source; }
    public boolean isConsumed() { return _consumed; }
    public void consume() { _consumed = true; }
    // used internally by Swingland to control event synthesis
    public boolean getCanSynthesize() { return _canSynthesize; }
    public void setCanSynthesize(boolean c) { _canSynthesize = c; }
    public void copyState(AbstractEvent clone) { _consumed = clone._consumed; _canSynthesize = clone._canSynthesize; }
    public abstract String toString();
    protected String pfxString() {
        return getClass().getSimpleName()+"(consumed="+_consumed+",canSynth="+_canSynthesize+",source="+_source.getClass().getSimpleName()+",id="+_id;
    }
}
