package com.ashbysoft.swingland;

/*
 * Bitmap fonts - lazy loaded, also used to hold cursor themes.
 * File format (offset and size in bytes, values little endian):
 * +----------------------------------------------------------+
 * | offset |  size  |  name  | description                   |
 * +----------------------------------------------------------+
 * |      0 |      1 | gwidth | glyph width in pixels/bits    |
 * |      1 |      1 | gheight| glyph height in pixels/bits   |
 * |      2 |      1 | gbase  | glyph baseline (pixels up)    |
 * |      3 |      1 | glead  | glyph leading (pixels apart)  |
 * |      4 |      4 | goffset| unicode offset of first glyph |
 * |      8 |      4 | gcount | count of glyphs in file       |
 * |     12 |      4 |gmissing| codepoint for missing glyphs  |
 * |     16 |see down|dataSize| glyph bitmaps, byte aligned   |
 * +----------------------------------------------------------+
 * each glyph occupies a whole number of bytes, calculated as:
 * bytesPerGlyph = (gwidth * gheight + 7) / 8;
 * dataSize in the table above is calculate as:
 * dataSize = bytesPerGlyph * gcount;
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class SwinglandFont extends Font implements FontMetrics {
    protected int _width;
    protected int _height;
    protected int _baseline;
    protected int _leading;
    protected int _glyphBytes;
    protected int _offset;
    protected int _count;
    protected int _missing;
    protected byte[] _buffer;

    public SwinglandFont(String name) { super(name); }
    protected boolean ensureLoaded() {
        if (_buffer != null)
            return true;
        return ensureImpl();
    }
    private synchronized boolean ensureImpl() {
        String fullPath = _name.startsWith("/") ? _name : FONT_PATH + _name;
        try (InputStream in = getClass().getResourceAsStream(fullPath)) {
            if (null == in)
                throw new IOException("unable to open font resource: "+fullPath);
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
            _log.info("lazy loaded: "+_name+": ("+gw+"x"+gh+"/"+gb+"/"+gl+"):"+go+"->"+(go+gc)+"/"+mg);
            return true;
        } catch (IOException e) {
            _log.error("unable to load font resource: "+FONT_PATH + _name + ": "+e.toString());
        }
        return false;
    }
    protected String[] findFonts() {
        ArrayList<String> rv = new ArrayList<>();
        // https://stackoverflow.com/questions/50469600/how-do-you-list-all-files-in-the-resources-folder-java-scala
        try (var fs = FileSystems.newFileSystem(getClass().getResource(FONT_PATH).toURI(), new HashMap<String,String>())) {
            var fp = fs.getPath(FONT_PATH);
            Files.list(fp).filter(p -> !p.getFileName().toString().contains(".")).forEach(
                p -> {
                    rv.add(p.toString());
                    _log.detail("findFonts(res): "+p.toString());
                }
            );
        } catch (IOException x) {
            _log.error("findFonts: unable to enumerate resources: "+x.toString());
        } catch (URISyntaxException x) {
            _log.error("findFonts: unable to enumerate resources: "+x.toString());
        }
        return rv.toArray(new String[0]);
    }
    protected String familyName() { return "Fixed"; }   // all Swingland fonts are fixed width
    protected int missingGlyph() { return _missing; }
    protected int mapCodePoint(int cp) {
        cp -= _offset;
        if (cp >= 0 && cp < _count)
            return cp;
        return -1;
    }
    protected int renderGlyph(RenderContext c, int gl) {
        gl = gl < 0 ? _missing : gl;
        int o = gl * _glyphBytes;
        int p = 7;
        for (int gy = 0; gy < _height; gy += 1) {
            for (int gx = 0; gx < _width; gx += 1) {
                if ((_buffer[o] & (1 << p)) != 0)
                    c.setPixel(gx, gy - _height);
                p -= 1;
                if (p < 0) {
                    p = 7;
                    o += 1;
                }
            }
        }
        return _width;
    }
    // FontMetrics API
    protected FontMetrics metrics() { return this; }
    public Font getFont() { return this; }
    public int getAscent() { return _height - _leading - _baseline; }
    public int getDescent() { return _baseline; }
    public int getHeight() { return _height; }
    public int getLeading() { return _leading; }
    public int stringWidth(String s) { return s.length() * _width; }
    public int charWidth(int c) { return _width; }
}
