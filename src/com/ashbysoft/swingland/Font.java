package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import java.util.HashMap;

public abstract class Font {
    public static final String FONT_PATH = "/fonts/";
    public static final String MONOSPACED = "MONOSPACED";
    private static final HashMap<String, Font> _fontCache = new HashMap<String, Font>();

    protected final Logger _log = new Logger("["+getClass().getSimpleName()+"]:");
    protected final String _name;

    protected Font(String name) { _name = name; }
    public String getFontName() { return _name; }
    public int getMissingGlyphCode() {
        if (!ensureLoaded())
            return -1;
        return missingGlyph();
    }
    public boolean canDisplay(char c) {
        return canDisplay((int)c);
    }
    public boolean canDisplay(int cp) {
        if (!ensureLoaded())
            return false;
        return mapCodePoint(cp) > -1;
    }
    public String toString() { return getClass().getSimpleName()+"("+_name+")"; }

    // package-private bitmap renderer, not the formal scalable fonts API in the JDK..which is *huge*
    void renderString(Graphics g, String s, int x, int y) {
        if (!ensureLoaded())
            return;
        int cx = 0;
        for (int i = 0; i < s.length(); i += 1) {
            int gl = mapCodePoint(s.codePointAt(i));
            if (gl < 0) gl = missingGlyph();
            cx += renderGlyph(g, gl, x+cx, y);
        }
    }
    void renderCodePoint(Graphics g, int cp, int x, int y) {
        if (!ensureLoaded())
            return;
        renderGlyph(g, mapCodePoint(cp), x, y);
    }

    // FontMetrics API
    // NB: getAscent()+getDescent()+getLeading() == getHeight()
    public FontMetrics getFontMetrics() {
        if (!ensureLoaded())
            return null;
        return metrics();
    }

    // Lazy loader
    protected abstract boolean ensureLoaded();
    // Implementation methods
    protected abstract int missingGlyph();                                  // glyph id to render if mapping does not exist
    protected abstract int mapCodePoint(int cp);                            // return glyph id, or -1 if not mappable
    protected abstract int renderGlyph(Graphics g, int gl, int x, int y);   // (x,y) => origin point on baseline, returns x advance to next origin
    protected abstract FontMetrics metrics();                               // return metrics for implementation (can be same instance)

    // Factory method
    public static Font getFont(String name) {
        // cached?
        synchronized (_fontCache) {
            if (!_fontCache.containsKey(name))
                _fontCache.put(name, createFont(name));
            return _fontCache.get(name);
        }
    }
    public static Font createFont(String name) {
        // PCF name?
        if (name.endsWith(".pcf") || name.endsWith("pcf.gz"))
            return new PCFFont(name);
        else
            return new SwinglandFont(name);
    }
}
