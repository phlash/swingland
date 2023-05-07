package com.ashbysoft.swingland.event;

public class MouseWheelEvent extends MouseEvent {
    private final int _clicks;
    public MouseWheelEvent(Object source, int id, int mask, int x, int y, int button, int state, int clicks) {
        super(source, id, mask, x, y, button, state);
        _clicks = clicks;
    }
    public int getWheelRotation() { return _clicks; }
}
