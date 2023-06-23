package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.geom.AffineTransform;

import java.util.HashMap;

public abstract class Font {
    public static final String FONT_PATH = "/fonts/";
    public static final String MONOSPACED = "MONOSPACED";
    private static final HashMap<String, Font> _fontCache = new HashMap<String, Font>();

    protected final Logger _log = new Logger("["+getClass().getSimpleName()+"]:");
    protected final String _name;

    protected class RenderContext {
        public final Graphics _g;
        private final AffineTransform _t;
        public final int _x, _y;
        public int _a;
        public RenderContext(Graphics g, AffineTransform t, int x, int y) { _g = g; _t = t; _x = x; _y = y; _a = 0; }
        public void setPixel(int x, int y) {
            // add accumulated advance width
            double[] tp = { (double)(x + _a), (double)y };
            // apply transform (if any)
            if (_t != null)
                tp = _t.transform(tp[0], tp[1]);
            // offset to origin
            int px = (int)tp[0] + _x;
            int py = (int)tp[1] + _y;
            // stay within graphics bounds
            Rectangle r = _g.getBounds();
            if (px >= 0 && px < r._w && py >= 0 && py < r._h)
                _g.setPixel(px, py);
        }
    }

    protected Font(String name) { _name = name; }
    public String getFontName() { return _name; }
    public AffineTransform getTransform() { return null; }
    public String getFamilyName() {
        if (!ensureLoaded())
            return null;
        return familyName();
    }
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
        RenderContext ctx = new RenderContext(g, getTransform(), x, y);
        for (int i = 0; i < s.length(); i += 1) {
            int gl = mapCodePoint(s.codePointAt(i));
            if (gl < 0) gl = missingGlyph();
            ctx._a += renderGlyph(ctx, gl);
        }
    }
    void renderCodePoint(Graphics g, int cp, int x, int y) {
        if (!ensureLoaded())
            return;
        RenderContext ctx = new RenderContext(g, getTransform(), x, y);
        renderGlyph(ctx, mapCodePoint(cp));
    }

    // package-private accessor for FontMetrics
    FontMetrics getFontMetrics() {
        if (!ensureLoaded())
            return null;
        return metrics();
    }

    // Lazy loader
    protected abstract boolean ensureLoaded();
    // Implementation methods
    protected abstract String[] findFonts();                                // return appropriate names for all available fonts of this type
    protected abstract String familyName();                                 // return font family name
    protected abstract int missingGlyph();                                  // glyph id to render if mapping does not exist
    protected abstract int mapCodePoint(int cp);                            // return glyph id, or -1 if not mappable
    protected abstract int renderGlyph(RenderContext ctx, int gl);          // returns advance width to next origin
    protected abstract FontMetrics metrics();                               // return metrics for implementation (can be same instance)

    // package-private cache preloader, reader and flush
    static void preloadCache() {
        // Swingland fonts (internal)
        for (var f : new SwinglandFont("").findFonts())
            getFont(f);
        // PCF fonts (on disk)
        for (var f : new PCFFont("").findFonts())
            getFont(f);
    }
    static Font[] getCache() {
        synchronized (_fontCache) {
            Font[] rv = new Font[_fontCache.size()];
            int i = 0;
            for (var e : _fontCache.values())
                rv[i++] = e;
            return rv;
        }
    }
    static void flushCache() {
        synchronized (_fontCache) {
            _fontCache.clear();
        }
    }

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
    public Font deriveFont(AffineTransform transform) {
        String derivedName = getFontName()+"-"+transform.toString();
        synchronized (_fontCache) {
            if (!_fontCache.containsKey(derivedName)) {
                _fontCache.put(derivedName, new DerivedFont(this, transform));
            }
            return _fontCache.get(derivedName);
        }
    }
    static class DerivedFont extends Font {
        private Font _base;
        private AffineTransform _transform;
        private DerivedFont(Font base, AffineTransform transform) {
            super(base.getFontName());
            _base = base;
            _transform = transform;
        }
        public AffineTransform getTransform() { return _transform; }
        protected boolean ensureLoaded() { return _base.ensureLoaded(); }
        protected String[] findFonts() { return null; }
        protected String familyName() { return _base.familyName(); }
        protected int missingGlyph() { return _base.missingGlyph(); }
        protected int mapCodePoint(int cp) { return _base.mapCodePoint(cp); }
        protected int renderGlyph(RenderContext ctx, int gl) { return _base.renderGlyph(ctx, gl); }
        protected FontMetrics metrics() { return _base.metrics(); }
    }
}
