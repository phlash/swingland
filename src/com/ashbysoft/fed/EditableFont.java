package com.ashbysoft.fed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.Font;

public class EditableFont extends Font {
    private Logger _log;
    private File _fontfile;
    private int _current;

    public EditableFont(Logger log, File fontfile, int w, int h, int b, int l, int o, int m) {
        super(fontfile.getName());
        _log = log;
        _fontfile = fontfile;
        _current = 0;
        if (_fontfile.exists()) {
            // existing font - load it in
            try (InputStream in = new FileInputStream(_fontfile)) {
                loadFont(in);
            } catch (IOException e) {
                _log.error(e.toString());
            }
        } else {
            // new font, initialise state, then add one empty glyph
            _width = w;
            _height = h;
            _glyphBytes = (w*h+7)/8;
            _baseline = b;
            _leading = l;
            _offset = o;
            _missing = m;
            _count = 0;
            _buffer = new byte[0];
            addGlyph(0);
        }
    }
    private void loadFont(InputStream in) throws IOException {
        // @see Font source for format details
        // see above for header definition
        byte[] hdr = new byte[16];
        if (in.read(hdr) != 16)
            throw new IOException("font resource < 16 bytes");
        int gw = (int)hdr[0] & 0xff;    // glyph width
        int gh = (int)hdr[1] & 0xff;    // glyph height
        int gb = (int)hdr[2] & 0xff;    // glyph baseline position
        int gl = (int)hdr[3] & 0xff;    // glyph leading (interline gap)
        // character offset (into unicode list)
        int go = ((int)hdr[4] & 0xff) | (((int)hdr[5] & 0xff) << 8) | (((int)hdr[6] & 0xff) << 16) | (((int)hdr[7] & 0xff) << 24);
        // character count (in file)
        int gc = ((int)hdr[8] & 0xff) | (((int)hdr[9] & 0xff) << 8) | (((int)hdr[10] & 0xff) << 16) | (((int)hdr[11] & 0xff) << 24);
        // missing glyph code (unicode value)
        int mg = ((int)hdr[12] & 0xff) | (((int)hdr[13] & 0xff) << 8) | (((int)hdr[14] & 0xff) << 16) | (((int)hdr[15] & 0xff) << 24);
        // calculate byte size of a glyph, and load them into buffer
        int bpg = (gw*gh+7) / 8;
        byte[] buf = new byte[bpg * gc];
        if (in.read(buf) != (bpg * gc))
            throw new IOException("font resource shorter than declared size: ("+gw+"x"+gh+"x"+gc+")");
        // all good - stash info
        _width = gw;
        _height = gh;
        _baseline = gb;
        _leading = gl;
        _glyphBytes = bpg;
        _offset = go;
        _count = gc;
        _missing = mg;
        _buffer = buf;
    }
    private void saveFont() {
        try (OutputStream out = new FileOutputStream(_fontfile)) {
            byte[] hdr = new byte[16];
            hdr[0] = (byte)_width;
            hdr[1] = (byte)_height;
            hdr[2] = (byte)_baseline;
            hdr[3] = (byte)_leading;
            hdr[4] = (byte)_offset;
            hdr[5] = (byte)(_offset >> 8);
            hdr[6] = (byte)(_offset >> 16);
            hdr[7] = (byte)(_offset >> 24);
            hdr[8] = (byte)_count;
            hdr[9] = (byte)(_count >> 8);
            hdr[10] = (byte)(_count >> 16);
            hdr[11] = (byte)(_count >> 24);
            hdr[12] = (byte)_missing;
            hdr[13] = (byte)(_missing >> 8);
            hdr[14] = (byte)(_missing >> 16);
            hdr[15] = (byte)(_missing >> 24);
            out.write(hdr);
            out.write(_buffer);
        } catch (IOException e) {
            _log.error(e.toString());
        }
    }
    public int getCurrent() { return _current; }
    public int setCurrent(int p) {
        // clip position
        if (p < 0) p = 0;
        if (p >= _count) p = _count - 1;
        _current = p;
        return _current;
    }
    public int getOffset() { return _offset; }
    public int getCount() { return _count; }
    public int addGlyph(int p) {
        // clip position
        if (p < 0) p = 0;
        if (p > _count) p = _count;
        int o = p *_glyphBytes;
        byte[] t = _buffer;
        _count += 1;
        _buffer = new byte[_count * _glyphBytes];
        // copy section before add position
        System.arraycopy(t, 0, _buffer, 0, o);
        // zero inserted glyph
        for (int i = 0; i < _glyphBytes; i += 1)
            _buffer[o+i] = (byte)0;
        // copy section after add position
        System.arraycopy(t, o, _buffer, o + _glyphBytes, t.length - o);
        // assume we want to edit the new glyph
        _current = p;
        // auto-save
        saveFont();
        return _count;
    }
    public int remGlyph(int p) {
        if (_count < 2) {
            _log.error("attempt to remove last glyph from font");
            return -1;
        }
        // clip position
        if (p < 0) p = 0;
        if (p >= _count) p = _count - 1;
        int o = p * _glyphBytes;
        byte[] t = _buffer;
        _count -= 1;
        _buffer = new byte[_count * _glyphBytes];
        // copy section before rem position
        System.arraycopy(t, 0, _buffer, 0, o);
        // copy section after rem position
        System.arraycopy(t, o + _glyphBytes, _buffer, o, t.length - o - _glyphBytes);
        // correct selection if we removed the last glyph
        if (_current >= _count)
            _current -= 1;
        // auto-save
        saveFont();
        return _count;
    }
    public void setGlyphPixel(int x, int y, boolean val) {
        getSetGlyphPixel(x, y, true, val);
        // auto-save
        saveFont();
    }
    public boolean getGlyphPixel(int x, int y) {
        return getSetGlyphPixel(x, y, false, false);
    }
    private boolean getSetGlyphPixel(int x, int y, boolean set, boolean val) {
        // clip co-ordinates
        if (x < 0) x = 0;
        if (x >= _width) x = _width - 1;
        if (y < 0) y = 0;
        if (y >= _height) y = _height - 1;
        // calculate byte in buffer, and bit of byte
        int o = (_width * y + x) / 8;
        int r = (_width * y + x) % 8;
        int p = _current * _glyphBytes + o;
        int b = 0x80 >> r;
        // get current value
        boolean rv = (_buffer[p] & b) != 0;
        // set/clear if requested
        if (set) {
            if (val)
                _buffer[p] |= (byte)b;
            else
                _buffer[p] &= (byte)~b;
        }
        return rv;
    }
}
