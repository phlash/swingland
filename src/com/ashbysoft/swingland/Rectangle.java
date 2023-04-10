package com.ashbysoft.swingland;

public class Rectangle {
    public final int _x;
    public final int _y;
    public final int _w;
    public final int _h;
    public Rectangle(int x, int y, int w, int h) {
        _x = x;
        _y = y;
        _w = w;
        _h = h;
    }
    public Rectangle offset(Rectangle delta) {
        return new Rectangle(
            _x + delta._x,
            _y + delta._y,
            delta._w,
            delta._h
        );
    }
    public String toString() { return "Rect("+_x+","+_y+","+_w+","+_h+")"; }
}
