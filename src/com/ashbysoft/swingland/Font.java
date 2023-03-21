package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

// Lazy loaded fonts - currently just fixed size bitmap fonts from internal resources
public class Font {
    public static final String FONT_PATH = "/fonts/";
    public static final String MONOSPACED = "MONOSPACED";

    private static final Logger _log = new Logger("[Font]:");
    private static final HashMap<String, Font> _fontCache = new HashMap<String, Font>();
    private String _name;
    private int _width;
    private int _height;
    private int _glyphBytes;
    private int _offset;
    private int _count;
    private int _missing;
    private byte[] _buffer;

    protected Font(String name) { _name = name; }
    public String getFontName() { return _name; }
    public int getMissingGlyphCode() { return _missing; }
    public boolean canDisplay(char c) {
        return canDisplay((int)c);
    }
    public boolean canDisplay(int codePoint) {
        if (!ensureLoaded())
            return false;
        if ((codePoint - _offset) < _count)
            return true;
        return false;
    }
    // package-private bitmap renderer, not the formal scalable fonts API in the JDK..which is *huge*
    void renderString(Graphics g, String s, int x, int y) {
        int cx = 0;
        for (int i = 0; i < s.length(); i += 1) {
            int cp = s.codePointAt(i);
            if (!canDisplay(cp))
                cp = getMissingGlyphCode();
            cp -= _offset;
            int o = cp * _glyphBytes;
            int p = 7;
            for (int gy = 0; gy < _height; gy += 1) {
                for (int gx = 0; gx < _width; gx += 1) {
                    if ((_buffer[o] & (1 << p)) != 0)
                        g.setPixel(x+gx+cx, y+gy-_height);
                    p -= 1;
                    if (p < 0) {
                        p = 7;
                        o += 1;
                    }
                }
            }
            cx += _width;
        }
    }
    // Lazy loader
    private synchronized boolean ensureLoaded() {
        if (_buffer != null)
            return true;
        try (InputStream in = getClass().getResourceAsStream(FONT_PATH + _name)) {
            // font header is four bytes, specifying: glyph dimensions (w x h), ASCII offset and glyph count (0=256)
            byte[] hdr = new byte[8];
            if (in.read(hdr) != 8)
                throw new IOException("font resource < 8 bytes");
            int gw = (int)hdr[0] & 0xff;
            int gh = (int)hdr[1] & 0xff;
            int go = (int)hdr[2] & 0xff;
            int gc = (int)hdr[3] & 0xff;
            if (0 == gc)
                gc = 256;
            int mg = ((int)hdr[4]) | ((int)hdr[5] << 8) | ((int)hdr[6] << 16) | ((int)hdr[7] << 24);
            // calculate byte size of a glyph, and load them into buffer
            int gb = (gw * gh) / 8;
            byte[] buf = new byte[gb * gc];
            if (in.read(buf) != (gb * gc))
                throw new IOException("font resource shorter than declared size: ("+gw+"x"+gh+"x"+gc+")");
            // all good - stash info
            _width = gw;
            _height = gh;
            _glyphBytes = gb;
            _offset = go;
            _count = gc;
            _missing = mg;
            _buffer = buf;
            _log.info("lazy loaded: "+_name+": ("+gw+"x"+gh+"):"+go+"->"+(go+gc)+"/"+mg);
            return true;
        } catch (IOException e) {
            _log.error("unable to load font resource: "+FONT_PATH + _name + ": "+e.toString());
        }
        return false;
    }

    // Factory method
    public static Font getFont(String name) {
        // cached?
        synchronized (_fontCache) {
            if (!_fontCache.containsKey(name))
                _fontCache.put(name, new Font(name));
            return _fontCache.get(name);
        }
    }
}
