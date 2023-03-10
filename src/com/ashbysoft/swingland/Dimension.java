package com.ashbysoft.swingland;

public class Dimension {
    public int _w;
    public int _h;
    public Dimension() { this(0, 1); }
    public Dimension(int w, int h) { this._w = w; this._h = h; }
    public Dimension(Dimension d) { this._w = d._w; this._h = d._h; }
    public Object clone() { return getSize(); }
    public boolean equals(Object obj) {
        if (obj instanceof Dimension) {
            Dimension d = (Dimension)obj;
            return d._w == _w && d._h == _h;
        }
        return false;
    }
    // assuming _h is usually the smaller value, and _w typically < 16k (huge displays)
    // then this ensures unique hash codes for all combinations of _w & _h.
    public int hashCode() { return (_h * 16001) + _w; }
    public double getHeight() { return (double)_h; }
    public double getWidth() { return (double)_w; }
    public Dimension getSize() { return new Dimension(this); }
    public void setSize(int w, int h) { _w = w; _h = h; }
    public void setSize(double w, double h) { _w = (int)w; _h = (int)h; }
    public void setSize(Dimension d) { _w = d._w; _h = d._h; }
    public String toString() { return String.format("(%dx%d)", _w, _h); }
}