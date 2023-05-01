package com.ashbysoft.swingland.event;

public abstract class InputEvent extends AbstractEvent {
    public static final int SHIFT_DOWN_MASK = 64;
    public static final int CTRL_DOWN_MASK = 128;
    public static final int META_DOWN_MASK = 256;   // aka, 'Window key'
    public static final int ALT_DOWN_MASK = 1024;
    public static final int ALT_GRAPH_DOWN_MASK = 8192;

    private int _mask;
    public InputEvent(Object src, int id, int mask) { super(src, id); _mask = mask; }
    public int getModifiersEx() { return _mask; }
    protected String pfxString() {
        return super.pfxString() + ",mods="+_mask;
    }
}
