package com.ashbysoft.swingland.geom;

import java.util.Arrays;

import com.ashbysoft.swingland.Point;

public class AffineTransform {
    // we hold the transform as a flat array in order: m00, m10, m01, m11, m02, m12 which allows direct copying during initialisation.
    // we default to the identity matrix.
    private double[] _tx = { 1.0, 0, 0, 1.0, 0, 0 };
    public AffineTransform() {}
    public AffineTransform(double[] v) {
        switch (v.length) {
            case 4 -> System.arraycopy(v, 0, _tx, 0, 4);
            case 6-> System.arraycopy(v, 0, _tx, 0, 6);
            default -> throw new IllegalArgumentException("Must supply 4 or 6 element initializer array");
        }
    }
    public AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
        _tx[0] = m00;
        _tx[1] = m10;
        _tx[2] = m01;
        _tx[3] = m11;
        _tx[4] = m02;
        _tx[5] = m12;
    }
    public void concatenate(AffineTransform t) {
        // applies transform 't' before our transform: this := [this]*[t]
        double[] r = {
            _tx[0] * t._tx[0] + _tx[2] * t._tx[1],
            _tx[1] * t._tx[0] + _tx[3] * t._tx[1],
            _tx[0] * t._tx[2] + _tx[2] * t._tx[3],
            _tx[1] * t._tx[2] + _tx[3] * t._tx[3],
            _tx[0] * t._tx[4] + _tx[2] * t._tx[5] + _tx[4],
            _tx[1] * t._tx[4] * _tx[3] * t._tx[5] + _tx[5]
        };
        _tx = r;
    }
    public void translate(Point p) {
        translate((double)p._x, (double)p._y);
    }
    public void translate(double x, double y) {
        concatenate(new AffineTransform(0, 0, 0, 0, x, y));
    }
    public void rotate(double theta) {
        // check for near quadrant and optimise
        // https://docs.oracle.com/javase/10/docs/api/java/awt/geom/AffineTransform.html
        double s = Math.sin(theta);
        double c = Math.cos(theta);
        if (1.0 == c)   // cos(theta) == 1.0 indicates theta ~= 0.0, so not a rotation :)
            return;
        else if (1.0 == s)
            quadrantRotate(1);
        else if (-1.0 == c)
            quadrantRotate(2);
        else if (-1.0 == s)
            quadrantRotate(3);
        else 
            concatenate(new AffineTransform(c, s, -s, c, 0, 0));
    }
    public void quadrantRotate(int n) {
        AffineTransform t = new AffineTransform(0, 1.0, -1.0, 0, 0, 0); // exact single quadrant rotate
        while (n > 0) {
            concatenate(t);
            n -= 1;
        }
    }
    public Point transform(Point p) {
        double[] tp = transform((double)p._x, (double)p._y);
        return new Point((int)tp[0], (int)tp[1]);
    }
    public double[] transform(double x, double y) {
        double[] tp = { _tx[0] * x + _tx[2] * y + _tx[4], _tx[1] * x + _tx[3] * y + _tx[5] };
        return tp;
    }
    public String toString() {
        return "AffineTransform"+Arrays.toString(_tx);
    }
}
