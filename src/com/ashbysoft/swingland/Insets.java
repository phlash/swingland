package com.ashbysoft.swingland;

public class Insets {
    public final int _t;
    public final int _l;
    public final int _b;
    public final int _r;
    public Insets(int t, int l, int b, int r) {
        _t = t;
        _l = l;
        _b = b;
        _r = r;
    }
    public String toString() {
        return "Insets:("+_l+","+_t+")-("+_b+","+_r+")";
    }
}
