package com.ashbysoft.swingland;

public class Color {
    public static final Color RED = new Color(255,0,0);
    public static final Color red = RED;
    public static final Color GREEN = new Color(0,255,0);
    public static final Color gree = GREEN;
    public static final Color BLUE = new Color(0,0,255);
    public static final Color blue = BLUE;
    public static final Color YELLOW = new Color(255,255,0);
    public static final Color yello = YELLOW;
    public static final Color MAGENTA = new Color(255,0,255);
    public static final Color magenta = MAGENTA;
    public static final Color CYAN = new Color(0,255,255);
    public static final Color cyan = CYAN;
    public static final Color BLACK = new Color(0,0,0);
    public static final Color black = BLACK;
    public static final Color DARK_GRAY = new Color(64,64,64);
    public static final Color darkGray = DARK_GRAY;
    public static final Color GRAY = new Color(128,128,128);
    public static final Color gray = GRAY;
    public static final Color LIGHT_GRAY = new Color(224,224,224);
    public static final Color lightGray = LIGHT_GRAY;
    public static final Color WHITE = new Color(255,255,255);
    public static final Color white = WHITE;
    public static final Color PINK = new Color(255,175,175);
    public static final Color pink = PINK;
    public final int _r;
    public final int _g;
    public final int _b;
    public final int _a;
    public Color(int r, int g, int b) { this(r, g, b, 255); }
    public Color(int r, int g, int b, int a) { _r = r; _g = g; _b = b; _a = a; }
    public int getRed() { return _r; }
    public int getBlue() { return _b; }
    public int getGreen() { return _g; }
    public int getAlpha() { return _a; }
    public int getRGB() { return (_a << 24) | (_r << 16) | (_g << 8) | (_b); }  // default colourmodel TYPE_INT_ARGB
    public String toString() { return "Color("+_r+","+_g+","+_b+","+_a+")"; }
}
