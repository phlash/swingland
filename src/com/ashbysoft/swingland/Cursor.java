package com.ashbysoft.swingland;

public class Cursor {
    public interface Resources {
        void destroy();
    }
    public static final int DEFAULT_CURSOR = 0;
    public static final int CROSSHAIR_CURSOR = 1;
    public static final int TEXT_CURSOR = 2;
    public static final int WAIT_CURSOR = 3;
    public static final int SW_RESIZE_CURSOR = 4;
    public static final int SE_RESIZE_CURSOR = 5;
    public static final int NW_RESIZE_CURSOR = 6;
    public static final int NE_RESIZE_CURSOR = 7;
    public static final int N_RESIZE_CURSOR = 8;
    public static final int S_RESIZE_CURSOR = 9;
    public static final int W_RESIZE_CURSOR = 10;
    public static final int E_RESIZE_CURSOR = 11;
    public static final int HAND_CURSOR = 12;
    public static final int MOVE_CURSOR = 13;
    // indexed by type below..
    private static final String[] _names = {
        "default", "crosshair", "text", "wait", "sw_resize", "se_resize", "nw_resize", "ne_resize", "n_resize", "s_resize", "w_resize", "e_resize", "hand", "move"
    };
    private static final Point[] _hotspots = {
        // these are percentages, see below for scaling to pixel sizes
        new Point(0,0), new Point(50,50), new Point(50,0), new Point(50,50),
        new Point(0,100), new Point(100,100), new Point(0,0), new Point(100,0),
        new Point(50,0), new Point(50,100), new Point(0,50), new Point(100,50),
        new Point(25,25), new Point(25,25)
    };
    // where we get our cursor images - a font containing all as codes 0->MOVE_CURSOR
    private static final String _defaultTheme = "/cursors/DEFAULT";
    private static String _currentTheme;
    public static void setTheme(String theme) { _currentTheme = theme; }

    private int _type;
    private Font _font;
    private Point _hotspot;
    protected String name;  // Yuk, but it's in the API
    private Resources _res;
    public Cursor(int type) {
        _type = type;
        _font = Font.getFont(_currentTheme != null ? _currentTheme : _defaultTheme);
        name = _names[type];
        Point p = _hotspots[_type];
        Dimension d = getSize();
        _hotspot = new Point((d._w*p._x)/100, (d._h*p._y)/100);
    }
    public int getType() { return _type; }
    public String getName() { return name; }
    public String toString() { return "Cursor("+name+")"; }

    // factories
    public static Cursor _cursors[] = new Cursor[MOVE_CURSOR+1];
    public static Cursor getDefaultCursor() {
        return getPredefinedCursor(DEFAULT_CURSOR);
    }
    public static Cursor getPredefinedCursor(int type) {
        if (null == _cursors[type])
            _cursors[type] = new Cursor(type);
        return _cursors[type];
    }
    public static Cursor getSystemCursor(String name) {
        for (int type=0; type <= MOVE_CURSOR; type += 1)
            if (_names[type].equalsIgnoreCase(name))
                return getPredefinedCursor(type);
        throw new RuntimeException("no such cursor: "+name);
    }

    // package-private render helpers
    Resources getResources() { return _res; }
    void setResources(Resources res) { _res = res; }
    Dimension getSize() {
        FontMetrics fm = _font.getFontMetrics();
        return new Dimension(fm.charWidth(0), fm.getHeight());
    }
    Point getHotspot() { return _hotspot; }
    void drawCursor(Graphics g) {
        g.setColor(Color.WHITE);
        _font.renderCodePoint(g, _type, 0, _font.getFontMetrics().getHeight());
    }
}
