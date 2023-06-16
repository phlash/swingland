package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Output extends WaylandObject<Output.Listener> {
    public interface Listener {
        boolean outputGeometry(int x, int y, int w, int h, int subpix, String make, String model, int trans);
        boolean outputMode(int flags, int w, int h, int refresh);
        boolean outputScale(int s);
        boolean outputName(String s);
        boolean outputDescription(String s);
        boolean outputDone();
    }
    public static final int RQ_RELEASE = 0;
    public static final int EV_GEOMETRY = 0;
    public static final int EV_MODE = 1;
    public static final int EV_DONE = 2;
    public static final int EV_SCALE = 3;
    public static final int EV_NAME = 4;
    public static final int EV_DESCRIPTION = 5;
    // subpixel layout
    public static final int SUBPIXEL_UNKNOWN = 0;
    public static final int SUBPIXEL_NONE = 1;
    public static final int SUBPIXEL_HORZ_RGB = 2;
    public static final int SUBPIXEL_HORZ_BGR = 3;
    public static final int SUBPIXEL_VERT_RGB = 4;
    public static final int SUBPIXEL_VERT_BGR = 5;
    // transforms
    public static final int TRANSFORM_NORMAL = 0;
    public static final int TRANSFORM_90 = 1;
    public static final int TRANSFORM_180 = 2;
    public static final int TRANSFORM_270 = 3;
    public static final int TRANSFORM_FLIPPED = 4;
    public static final int TRANSFORM_FLIPPED_90 = 5;
    public static final int TRANSFORM_FLIPPED_180 = 6;
    public static final int TRANSFORM_FLIPPED_270 = 7;
    // mode flag bits
    public static final int MODE_CURRENT = 0x1;
    public static final int MODE_PREFERRED = 0x2;
    
    private int _gx;
    private int _gy;
    private int _gw;
    private int _gh;
    private int _sp;
    private int _tr;
    private int _mw;
    private int _mh;
    private int _mr;
    private int _sc;
    private String _make;
    private String _model;
    private String _name;
    private String _description;
    private boolean _done = false;

    public Output(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_GEOMETRY == op) {
            _gx = b.getInt();
            _gy = b.getInt();
            _gw = b.getInt();
            _gh = b.getInt();
            _sp = b.getInt();
            _make = getString(b);
            _model = getString(b);
            _tr = b.getInt();
            log(true, "geometry("+_gx+","+_gy+","+_gw+","+_gh+","+_sp+","+_make+","+_model+","+_tr+")");
            for (Listener l : listeners())
                if (!l.outputGeometry(_gx, _gy, _gw, _gh, _sp, _make, _model, _tr))
                    rv = false;
        } else if (EV_MODE == op) {
            int f = b.getInt();
            _mw = b.getInt();
            _mh = b.getInt();
            _mr = b.getInt();
            log(true, "mode("+f+","+_mw+","+_mh+","+_mr+")");
            for (Listener l : listeners())
                if (!l.outputMode(f, _mw, _mh, _mr))
                    rv = false;
        } else if (EV_SCALE == op) {
            _sc = b.getInt();
            log(true, "scale("+_sc+")");
            for (Listener l : listeners())
                if (!l.outputScale(_sc))
                    rv = false;
        } else if (EV_NAME == op) {
            _name = getString(b);
            log(true, "name("+_name+")");
            for (Listener l : listeners())
                if (!l.outputName(_name))
                    rv = false;
        } else if (EV_DESCRIPTION == op) {
            _description = getString(b);
            log(true, "description("+_description+")");
            for (Listener l : listeners())
                if (!l.outputDescription(_description))
                    rv = false;
        } else if (EV_DONE == op) {
            _done = true;
            log(true, "done");
            for (Listener l : listeners())
                if (!l.outputDone())
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }
    public boolean isDone() { return _done; }
    public int getGeometryX() { return _gx; }
    public int getGeometryY() { return _gy; }
    public int getGeometryW() { return _gw; }
    public int getGeometryH() { return _gh; }
    public int getSubpixelLayout() { return _sp; }
    public int getScreenTransform() { return _tr; }
    public int getModeWidth() { return _mw; }
    public int getModeHeight() { return _mh; }
    public int getRefreshRate() { return _mr; }
    public int getScaleFactor() { return _sc; }
    public String getScreenMake() { return _make; }
    public String getScreenModel() { return _model; }
    public String getName() { return _name; }
    public String getDescription() { return _description; }
    public boolean release() {
        log(false, "release()");
        ByteBuffer b = newBuffer(0, RQ_RELEASE);
        return _display.write(b);
    }
}
