package com.ashbysoft.swingland.event;

public class WindowEvent extends AbstractEvent {
    // event IDs
    public static final int WINDOW_OPENED = 0;
    public static final int WINDOW_CLOSED = 1;

    public WindowEvent(Object src, int id) { super(src, id); }
    public String toString() {
        return pfxString()+")";
    }
}
