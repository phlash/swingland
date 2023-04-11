package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;

import java.nio.ByteBuffer;

public class Graphics {
    private static Logger _log = new Logger("[Graphics]:");
    private ByteBuffer _buffer;
    private int _width;
    private int _height;
    private Rectangle _bounds;
    private Color _color;
    private Font _font;

    Graphics(ByteBuffer b, int w, int h, Color c, Font f) {
        _log.info("<init>:b="+b.toString()+" w="+w+" h="+h);
        _buffer = b;
        _width = w;
        _height = h;
        _bounds = new Rectangle(0, 0, _width, _height);
        _color = c != null ? c : Color.MAGENTA;
        _font = f != null ? f : Font.getFont(Font.MONOSPACED);
    }
    public Rectangle getBounds() { return _bounds; }
    public void setBounds(Rectangle r) {
        _log.info("bounds:x="+r._x+" y="+r._y+" w="+r._w+" h="+r._h);
        _bounds = r;
    }
    public Font getFont() { return _font; }
    public void setFont(Font font) { _font = font; }
    public void setColor(Color c) { _log.info("setColor:"+c); _color = c; }
    public void drawLine(int x1, int y1, int x2, int y2) {
        _log.info("drawLine:("+x1+","+y1+")->("+x2+","+y2+")");
        int dx = x2 - x1;
        int dy = y2 - y1;
        int sx = dx < 0 ? -1 : 1;
        int sy = dy < 0 ? -1 : 1;
        if (0 == dy) {
            // horizontal
            int x;
            for (x = x1; x != x2; x += sx)
                setPixel(x, y1);
            setPixel(x, y1);
        } else if (0 == dx) {
            // vertical
            int y;
            for (y = y1; y != y2; y += sy)
                setPixel(x1, y);
            setPixel(x1, y);
        } else if (0 == dy/dx) {
            // low slope, iterate X
            int x;
            for (x = x1; x != x2; x += sx)
                setPixel(x, y1 + ((x - x1) * dy) / dx);
            setPixel(x, y1 + ((x - x1) * dy) / dx);
        } else {
            // high slope, iterate Y
            int y;
            for (y = y1; y != y2; y += sy)
                setPixel(((y - y1) * dx) / dy, y);
            setPixel(((y - y1) * dx) / dy, y);
        }
    }
    public void drawRect(int x, int y, int w, int h) {
        _log.info("drawRect:("+w+"x"+h+")@("+x+","+y+")");
        for (int ox = 0; ox < w; ox += 1) {
            setPixel(x+ox, y);
            setPixel(x+ox, y+h-1);
        }
        for (int oy = 0; oy < h; oy += 1) {
            setPixel(x, y+oy);
            setPixel(x+w-1, y+oy);
        }
    }
    public void fillRect(int x, int y, int w, int h) {
        _log.info("fillRect:("+w+"x"+h+")@("+x+","+y+")");
        for (int oy = 0; oy < h; oy += 1)
            for (int ox = 0; ox < w; ox += 1)
                setPixel(x+ox, y+oy);
    }
    public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
        _log.info("drawRoundRect("+w+"x"+h+")@("+x+","+x+"):aw="+aw+",ah="+ah);
        int hw = aw/2;
        int hh = ah/2;
        drawLine(x+hw, y, x+w-hw, y);
        drawLine(x+hw, y+h-1, x+w-hw, y+h-1);
        drawLine(x, y+hh, x, y+h-hh-1);
        drawLine(x+w-1, y+hh, x+w-1, y+h-hh-1);
        oval(x+w/2-hw, y+h/2-hh, aw, ah, false, w-aw, h-ah);
    }
    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
        _log.info("fillRoundRect("+w+"x"+h+")@("+x+","+x+"):aw="+aw+",ah="+ah);
        int hw = aw/2;
        int hh = ah/2;
        fillRect(x+hw, y, w-aw, hh);
        fillRect(x, y+hh, w, h-ah);
        fillRect(x+hw, y+h-hh-1, w-aw, ah-hh);
        oval(x+w/2-hw, y+h/2-hh, aw, ah, true, w-aw, h-ah);
    }
    public void drawOval(int x, int y, int w, int h) {
        oval(x, y, w, h, false, 0, 0);
    }
    public void fillOval(int x, int y, int w, int h) {
        oval(x, y, w, h, true, 0, 0);
    }
    private class Quarters {
        public final Point _tl;
        public final Point _tr;
        public final Point _bl;
        public final Point _br;
        public Quarters(Point tl, Point tr, Point bl, Point br) { _tl=tl; _tr=tr; _bl=bl; _br=br; }
    };
    private void oval(int x, int y, int w, int h, boolean filled, int sw, int sh) {
        _log.info("oval("+w+"x"+h+")@("+x+","+y+"):f="+filled+",sw="+sw+",sh="+sh);
        // pre-calculate ellipse axis radii
        int a = w/2;
        int b = h/2;
        // pre-calculate origins for each quarter, including spread factors
        Quarters q = new Quarters(
            new Point(x+a-sw/2, y+b-sh/2),
            new Point(x+a+sw/2, y+b-sh/2),
            new Point(x+a-sw/2, y+b+sh/2),
            new Point(x+a+sw/2, y+b+sh/2)
        );
        // apply standard equation: (x*x / a*a) + (y*y / b*b) == 1
        // cross multiply: (x*x * b*b) + (y*y * a*a) == a*a * b*b
        // - allows integer math, avoids division.
        // - scan lower right quarter, plot at / within boundary
        // - reflect into other three quarters
        long a2 = a*a;
        long b2 = b*b;
        long d2 = a2*b2;
        int lx = a;
        for (int oy = 0; oy <= b; oy += 1) {
            long y2a2 = oy*oy * a2;
            // scan right-to-left, from last x (lx) in line above, for efficiancy
            for (int ox = lx; ox >= 0; ox -= 1) {
                long dt = (ox*ox * b2) + y2a2;
                // at or below boundary?
                if (dt <= d2) {
                    // draw line back to last x
                    plotQuarters(q, ox, oy);
                    int hd = (lx-ox)/2;
                    for (int dx = 1; dx < hd; dx += 1) {
                        plotQuarters(q, ox+dx, oy);
                    }
                    for (int dx = hd > 1 ? hd : 1; dx < (lx-ox); dx += 1) {
                        plotQuarters(q, ox+dx, oy-1);
                    }
                    lx = ox;
                    // filled - draw line back to zero
                    if (filled)
                        for (ox -= 1; ox >= 0; ox -= 1)
                            plotQuarters(q, ox, oy);
                    else
                        ox = -1;
                }
            }
        }
    }
    private void plotQuarters(Quarters q, int ox, int oy) {
        // plot 4 quarters
        setPixel(q._tl._x-ox, q._tl._y-oy); // TL
        setPixel(q._tr._x+ox, q._tr._y-oy); // TR
        setPixel(q._bl._x-ox, q._bl._y+oy); // BL
        setPixel(q._br._x+ox, q._br._y+oy); // BR
    }
    public void drawString(String s, int x, int y) {
        _log.info("drawString:("+x+","+y+"):"+s);
        _font.renderString(this, s, x, y);
    }
    public void drawChars(char[] data, int o, int l, int x, int y) {
        String s = new String(data, o, l);
        drawString(s, x, y);
    }
    // package-private pixel setter, not part of public API
    void setPixel(int x, int y) {
        // apply bounds offset
        x += _bounds._x;
        y += _bounds._y;
        // calculate buffer position, assume: width==stride, format==ARGB
        int o = (y * _width + x) * 4;
        // noisily discard offsets outside the buffer!
        if (o < 0 || o >= _buffer.limit()) {
            _log.error("setPixel outside buffer: x/w,y/h,o/limit: "+x+"/"+_width+","+y+"/"+_height+","+o+"/"+_buffer.limit());
            return;
        }
        // convert color to ARGB pixel
        int c = (_color._a << 24) | (_color._r << 16) | (_color._g << 8) | (_color._b);
        _buffer.putInt(o, c);
    }
}
