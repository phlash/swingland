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
    public void setColor(Color c) { _color = c; }
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
            setPixel(x+ox, y+h);
        }
        for (int oy = 0; oy < h; oy += 1) {
            setPixel(x, y+oy);
            setPixel(x+w, y+oy);
        }
    }
    public void fillRect(int x, int y, int w, int h) {
        _log.info("fillRect:("+w+"x"+h+")@("+x+","+y+")");
        for (int oy = 0; oy < h; oy += 1)
            for (int ox = 0; ox < w; ox += 1)
                setPixel(ox, oy);
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
        // convert color to ARGB pixel
        int c = (_color._a << 24) | (_color._r << 16) | (_color._g << 8) | (_color._b);
        _buffer.putInt(o, c);
    }
}
