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
    public Rectangle union(Rectangle other) {
        return new Rectangle(
            Integer.min(_x, other._x),
            Integer.min(_y, other._y),
            Integer.max(_x + _w, other._x + other._w) - Integer.min(_x, other._x),
            Integer.max(_y + _h, other._y + other._h) - Integer.min(_y, other._y)
        );
    }
    public Rectangle intersection(Rectangle other) {
        if ((_x + _w) < other._x ||
            (other._x + other._w) < _x ||
            (_y + _h) < other._y ||
            (other._y + other._h) < _y)
            return null;
        return new Rectangle(
            Integer.max(_x, other._x),
            Integer.max(_y, other._y),
            Integer.min(_x + _w, other._x + other._w) - Integer.max(_x, other._x),
            Integer.min(_y + _h, other._y + other._h) - Integer.max(_y, other._y)
        );
    }
    public String toString() { return "Rect("+_x+","+_y+","+_w+","+_h+")"; }
}
