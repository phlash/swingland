// XXX:TODO

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

    Graphics(ByteBuffer b, int w, int h) {
        _log.info("<init>:b="+b.toString()+" w="+w+" h="+h);
        _buffer = b;
        _width = w;
        _height = h;
        _bounds = new Rectangle(0, 0, _width, _height);
        _color = Color.MAGENTA;
        _font = Font.getFont(Font.MONOSPACED);
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
        _log.info("drawRect:part2");
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
        drawLine(x, y+hh, x, y+h-hh);
        drawLine(x+w-1, y+hh, x+w-1, y+h-hh);
        oval(x+w/2-hw, y+h/2-hh, aw, ah, false, w-aw, h-ah);
    }
    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
        _log.info("fillRoundRect("+w+"x"+h+")@("+x+","+x+"):aw="+aw+",ah="+ah);
        int hw = aw/2;
        int hh = ah/2;
        fillRect(x+hw, y, w-aw, hh);
        fillRect(x, hh, w, h-hh);
        fillRect(x+hw, y+h-hh, x-aw, hh);
        oval(x+w/2-hw, y+h/2-hh, aw, ah, true, w-aw, h-ah);
    }
    public void drawOval(int x, int y, int w, int h) {
        oval(x, y, w, h, false, 0, 0);
    }
    public void fillOval(int x, int y, int w, int h) {
        oval(x, y, w, h, true, 0, 0);
    }
    private void oval(int x, int y, int w, int h, boolean filled, int sw, int sh) {
        _log.info("oval("+w+"x"+h+")@("+x+","+y+"):sw="+sw+",sh="+sh);
        // pre-calculate ellipse axis radii
        int a = w/2;
        int b = h/2;
        // pre-calculate origins for each quarter, including spread factors
        int tlx = x+a-sw/2;
        int tly = y+b-sh/2;
        int trx = x+a+sw/2;
        int trY = y+b-sh/2; // stupid reserved words ;-)
        int blx = x+a-sw/2;
        int bly = y+b+sh/2;
        int brx = x+a+sw/2;
        int bry = y+b+sh/2;
        // apply standard equation: (x*x / a*a) + (y*y / b*b) == 1
        // cross multiply: (x*x * b*b) + (y*y * a*a) == a*a * b*b
        // - allows integer math, avoids division.
        // - scan lower right quarter, plot at / within boundary
        // - reflect into other three quarters
        long a2 = a*a;
        long b2 = b*b;
        long d2 = a2*b2;
        int lx = a-1;
        for (int oy = 0; oy <= b; oy += 1) {
            boolean in = true;
            for (int ox = 0; ox <= a; ox += 1) {
                long dt = (ox*ox * b2) + (oy*oy * a2);
                if (dt >= d2) {
                    // outside - skip plot unless not filled and still inside
                    if (filled || !in)
                        continue;
                    in = false;
                } else {
                    // inside - skip plot unless filled
                    if (!filled)
                        continue;
                }
                if (!filled && (lx - ox) > 1) {
                    // draw lines to fill out missing x points
                    drawLine(tlx-ox, tly-oy, tlx-lx, tly-oy+1); // TL
                    drawLine(trx+ox, trY-oy, trx+lx, trY-oy+1); // TR
                    drawLine(blx-ox, bly+oy, blx-lx, bly+oy-1); // BL
                    drawLine(brx+ox, bry+oy, brx+lx, bry+oy-1); // BR
                } else {
                    // plot 4 quarters
                    setPixel(tlx-ox, tly-oy); // TL
                    setPixel(trx+ox, trY-oy); // TR
                    setPixel(blx-ox, bly+oy); // BL
                    setPixel(brx+ox, bry+oy); // BR
                }
                lx = ox;
            }
        }
    }
    public void drawString(String s, int x, int y) {
        _log.info("drawString:("+x+","+y+"):"+s);
        _font.renderString(this, s, x, y);
    }
    // package-private pixel setter, not part of public API
    void setPixel(int x, int y) {
        // apply bounds offset
        x += _bounds._x;
        y += _bounds._y;
        // calculate buffer position, assume: width==stride, format==ARGB
        int o = (y * _width + x) * 4;
        // silently discard offsets outside the buffer!
        if (o < 0 || o >= _buffer.limit())
            return;
        // convert color to ARGB pixel
        int c = (_color._a << 24) | (_color._r << 16) | (_color._g << 8) | (_color._b);
        _buffer.putInt(o, c);
    }
}
