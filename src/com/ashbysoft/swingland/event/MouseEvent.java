package com.ashbysoft.swingland.event;

public class MouseEvent extends AbstractEvent {
    // event IDs
    public static final int MOUSE_MOVE = 0;
    public static final int MOUSE_BUTTON = 1;
    public static final int MOUSE_ENTERED = 2;
    public static final int MOUSE_EXITED = 3;
    public static final int MOUSE_CLICKED = 4;
    public static final int MOUSE_DRAGGED = 5;
    // button state
    public static final int BUTTON_RELEASED = 0;
    public static final int BUTTON_PRESSED = 1;
    private final int _x;
    private final int _y;
    private final int _button;
    private final int _state;
    public MouseEvent(Object source, int id, int x, int y, int button, int state) {
        super(source, id);
        _x = x;
        _y = y;
        _button = button;
        _state = state;
    }
    public int getX() { return _x; }
    public int getY() { return _y; }
    public int getButton() { return _button; }
    public int getState() { return _state; }
    public String toString() {
        return pfxString()+",x="+_x+",y="+_y+",button="+_button+",state="+_state+")";
    }
}
