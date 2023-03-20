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

    Graphics(ByteBuffer b, int w, int h) {
        _log.info("<init>:b="+b.toString()+" w="+w+" h="+h);
        _buffer = b;
        _width = w;
        _height = h;
        _bounds = new Rectangle(0, 0, _width, _height);
    }
    public Rectangle getBounds() { return _bounds; }
    public void setBounds(Rectangle r) {
        _log.info("bounds:x="+r._x+" y="+r._y+" w="+r._w+" h="+r._h);
        _bounds = r;
    }
    public void setColor(Color c) { _color = c; }
    public void drawLine(int x1, int y1, int x2, int y2) {}
    public void drawRect(int x, int y, int w, int h) {
        int tx = _bounds._x + x;
        int ty = _bounds._y + y;
        _log.info("drawRect:tx="+tx+" ty="+ty+" w="+w+" h="+h);
        for (int ox = 0; ox < w; ox += 1) {
            setPixel(tx+ox, ty);
            setPixel(tx+ox, ty+h);
        }
        for (int oy = 0; oy < h; oy += 1) {
            setPixel(tx, ty+oy);
            setPixel(tx+w, ty+oy);
        }
    }
    public void fillRect(int x, int y, int w, int h) {
        int tx = _bounds._x + x;
        int ty = _bounds._y + y;
        _log.info("fillRect:tx="+tx+" ty="+ty+" w="+w+" h="+h);
        for (int oy = 0; oy < h; oy += 1)
            for (int ox = 0; ox < w; ox += 1)
                setPixel(ox, oy);
    }
    public void drawString(String s, int x, int y) {}
    private void setPixel(int x, int y) {
        // we assume: width==stride, format==ARGB
        int o = y * _width + x;
        int c = 0xff000000 | (_color._r << 16) | (_color._g << 8) | (_color._b);
        _buffer.putInt(o * 4, c);
    }
}
