package com.ashbysoft.swingland;

// Based on specification: https://fontforge.org/docs/techref/pcf-format.html
// with considerable help from: https://github.com/tdm/android-x-server/blob/master/src/tdm/xserver/FontDataPCF.java

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class PCFFont extends Font implements FontMetrics {
    // tables types
    private static final int PCF_PROPERTIES = (1 << 0);
    private static final int PCF_METRICS = (1 << 2);
    private static final int PCF_BITMAPS = (1 << 3);
    private static final int PCF_BDF_ENCODINGS = (1 << 5);
    // table format bits
    private static final int PCF_BYTE_MASK = (1 << 2);   // indicates big endian
    private static final int PCF_COMPRESSED_METRICS = (1 << 8);
    private static final int PCF_GLYPH_PAD_MASK = (3 << 0);
    private static final int PCF_SCAN_UNIT_MASK = (3 << 4);

    // PCF table of contents
    class TOCEntry {
        public int type, format, size, offset;
        public TOCEntry(int t, int f, int s, int o) { type = t; format = f; size = s; offset = o; }
    }
    private HashMap<Integer, TOCEntry> _toc = new HashMap<>();
    // PCF font properties
    class PCFProp {
        public int offset, value;
        boolean isString;
        public PCFProp(int o, boolean s, int v) { offset = o; isString = s; value = v; }
    }
    private HashMap<Integer, PCFProp> _props = new HashMap<>();
    private HashMap<Integer, String> _pstrs = new HashMap<>();
    private String _family;
    // PCF font metrics
    class PCFMetrics {
        public int lbearing, rbearing, width, ascent, descent, attributes;
        public PCFMetrics(int l, int r, int w, int a, int d, int t) { lbearing = l; rbearing = r; width = w; ascent = a; descent = d; attributes = t; }
        public PCFMetrics max(PCFMetrics o) {
            int l = lbearing < o.lbearing ? lbearing : o.lbearing;
            int r = rbearing > o.rbearing ? rbearing : o.rbearing;
            int w = width > o.width ? width : o.width;
            int a = ascent > o.ascent ? ascent : o.ascent;
            int d = descent > o.descent ? descent : o.descent;
            return new PCFMetrics(l, r, w, a, d, attributes);
        }
    }
    private PCFMetrics[] _metrics = null;
    private PCFMetrics _maxMetrics = null;
    // PCF bitmap data
    class PCFBitmap {
        public int span, unit, offset;
        boolean lsbit, lsbyte;
        public PCFBitmap(int s, int u, int o, boolean i, boolean y) { span = s; unit = u; offset = o; lsbit = i; lsbyte = y; }
    }
    private PCFBitmap[] _bitmaps = null;
    private byte[] _rawbits = null;
    // PCF encodings table (2 dimensional for 2-byte encodings)
    class PCFEncoding {
        public int minb1, maxb1, minb2, maxb2, dflt;
        public int[][] map;
        public PCFEncoding(int mb1, int xb1, int mb2, int xb2, int d, int[][] e) { minb1 = mb1; maxb1 = xb1; minb2 = mb2; maxb2 = xb2; dflt = d; map = e; }
    }
    private PCFEncoding _encoding = null;
    private boolean _loaded = false;

    
    public PCFFont(String name) { super(name); }
    protected boolean ensureLoaded() {
        return _loaded || ensureImpl();
    }
    private synchronized boolean ensureImpl() {
        try (InputStream is = findFont(_name)) {
            int pos = 0;
            // header
            int id = getI(is, 4, false);
            if (id != 0x70636601)   // 'pcf.'
                throw new IOException("Not a PCF font file");
            int tcnt = getI(is, 4, false);
            pos += 8;
            _log.detail("- tables: "+tcnt);
            int max = 0;
            for (int i = 0; i < tcnt; i += 1) {
                int t = getI(is, 4, false);
                int f = getI(is, 4, false);
                int s = getI(is, 4, false);
                int o = getI(is, 4, false);
                pos += 16;
                _log.detail("\ttype:0x"+Integer.toHexString(t)+", format:0x"+Integer.toHexString(f)+", size:0x"+Integer.toHexString(s)+", offset:0x"+Integer.toHexString(o));
                _toc.put(o, new TOCEntry(t, f, s, o));
                if (o + s > max)
                    max = o + s;
            }
            while (pos < max) {
                // 32bit align for next table
                if ((pos % 4) > 0) {
                    getI(is, 4 - (pos % 4), false);
                    pos += 4 - (pos % 4);
                }
                // done?
                if (pos >= max)
                    break;
                // check we are on a table start position
                if (!_toc.containsKey(pos))
                    throw new IOException("Not at a table start: 0x"+Integer.toHexString(pos));
                TOCEntry te = _toc.get(pos);
                _log.detail("- @0x"+Integer.toHexString(pos));
                switch (te.type) {
                    case PCF_PROPERTIES -> pos += pcfProperties(is, te.size, te.format);
                    case PCF_METRICS -> pos += pcfMetrics(is, te.size, te.format);
                    case PCF_BITMAPS -> pos += pcfBitmaps(is, te.size, te.format);
                    case PCF_BDF_ENCODINGS -> pos += pcfEncodings(is, te.size, te.format);
                    default -> pos += skip(is, te.size);
                }
            }
            int en = _encoding.map.length * _encoding.map[0].length;
            _log.info("lazy loaded: "+_name+" max="+_maxMetrics.width+"x"+(_maxMetrics.ascent+_maxMetrics.descent)+" encs="+en+" glyphs="+_metrics.length);
            _loaded = true;
        } catch (IOException ex) {
            _log.error("Unable to load PCF font: "+ _name+": "+ex.toString());
        }
        return _loaded;
    }
    private String getXdgPath() {
        // First, try absolute path (empty first path component), then look in $XDG_DATA_DIRS
        String xdg = System.getenv("XDG_DATA_DIRS");
        if (null == xdg)
            xdg = ":/usr/local/share:/usr/share";
        else
            xdg = ":"+xdg;
        return xdg;
    }
    private InputStream findFont(String name) throws IOException {
        String fullPath = _name.startsWith("/") ? _name : FONT_PATH + _name;
        InputStream is = null;
        for (String s: getXdgPath().split(":")) {
            File f = new File(s + fullPath);
            if (f.canRead()) {
                is = new FileInputStream(f);
                break;
            }
        }
        if (null == is)
            is = getClass().getResourceAsStream(fullPath);
        if (null == is)
            throw new IOException("font not found in XDG_DATA_DIRS or as a resource: "+_name);
        if (name.endsWith(".gz"))
            is = new GZIPInputStream(is);
        return new BufferedInputStream(is);
    }
    private int pcfProperties(InputStream is, int size, int format) throws IOException {
        int fmt = getI(is, 4, false);
        int pos = 4;
        boolean big = (fmt & PCF_BYTE_MASK) > 0;     // PCF_BYTE_MASK
        if (fmt != format)
            throw new IOException("props: mismatched format: 0x"+Integer.toHexString(fmt)+"!=0x"+Integer.toHexString(format));
        int nprp = getI(is, 4, big);
        pos += 4;
        _log.detail("- props: big="+big+", nprops="+nprp);
        for (int i = 0; i < nprp; i += 1) {
            int off = getI(is, 4, big);
            int isS = getI(is, 1, big);
            int val = getI(is, 4, big);
            pos += 9;
            //log("\toff=0x"+Integer.toHexString(off)+", isString="+isS+", value=0x"+Integer.toHexString(val));
            _props.put(off, new PCFProp(off, isS != 0, val));
        }
        // pad to next 32bit boundary
        if ((pos % 4) > 0) {
            getI(is, 4 - (pos % 4), big);
            pos += 4 - (pos % 4);
        }
        int slen = getI(is, 4, big);
        pos += 4;
        byte[] sbuf = new byte[slen];
        if (is.read(sbuf) != slen)
            throw new IOException("short read of string table");
        pos += slen;
        int sidx = 0;
        int pidx = 0;
        while (sidx < slen) {
            // find the next \0 or reach slen
            while (sidx < slen && sbuf[sidx] != 0) sidx += 1;
            // stash the string
            if (sidx > pidx)
                _pstrs.put(pidx, new String(sbuf, pidx, sidx - pidx));
            else
                _pstrs.put(pidx, "");
            // move along
            sidx += 1;
            pidx = sidx;
        }
        for (var p : _props.values()) {
            if (_pstrs.get(p.offset).equals("FAMILY_NAME"))
                _family = _pstrs.get(p.value);
            _log.detail("\t"+_pstrs.get(p.offset)+"="+ (p.isString ? "\""+_pstrs.get(p.value)+"\"" : "0x"+Integer.toHexString(p.value)));
        }
        if (null == _family)
            _family = "Unknown";
        return pos;
    }
    private int pcfMetrics(InputStream is, int size, int format) throws IOException {
        int fmt = getI(is, 4, false);
        int pos = 4;
        if (fmt != format)
            throw new IOException("metrics: mismatched format: 0x"+Integer.toHexString(fmt)+"!=0x"+Integer.toHexString(format));
        boolean big = (fmt & PCF_BYTE_MASK) > 0;
        boolean comp = (fmt & PCF_COMPRESSED_METRICS) > 0;
        int nmet = getI(is, comp ? 2 : 4, big);
        pos += comp ? 2 : 4;
        _log.detail("- metrics: big="+big+", comp="+comp+", nmetrics="+nmet);
        if (_bitmaps != null && _bitmaps.length != nmet)
            throw new IOException("metrics: mismatched glyph count with bitmaps: "+nmet+"!="+_bitmaps.length);
        _metrics = new PCFMetrics[nmet];
        for (int i = 0; i < nmet; i += 1) {
            int lbrg = getI(is, comp ? 1 : 2, big); lbrg -= comp ? 128 : 0;
            int rbrg = getI(is, comp ? 1 : 2, big); rbrg -= comp ? 128 : 0;
            int cwid = getI(is, comp ? 1 : 2, big); cwid -= comp ? 128 : 0;
            int casc = getI(is, comp ? 1 : 2, big); casc -= comp ? 128 : 0;
            int cdsc = getI(is, comp ? 1 : 2, big); cdsc -= comp ? 128 : 0;
            int attr = comp ? 0 : getI(is, 2, big);
            pos += comp ? 5 : 12;
            _metrics[i] = new PCFMetrics(lbrg, rbrg, cwid, casc, cdsc, attr);
            _maxMetrics = _maxMetrics != null ? _maxMetrics.max(_metrics[i]) : _metrics[i];
        }
        return pos;
    }
    private int pcfBitmaps(InputStream is, int size, int format) throws IOException {
        int fmt = getI(is, 4, false);
        int pos = 4;
        if (fmt != format)
            throw new IOException("bitmaps: mismatched format: 0x"+Integer.toHexString(fmt)+"!=0x"+Integer.toHexString(format));
        boolean big = (fmt & PCF_BYTE_MASK) > 0;
        int bidx = fmt & PCF_GLYPH_PAD_MASK;
        int scan = (fmt & PCF_SCAN_UNIT_MASK) >> 4;
        int ngly = getI(is, 4, big);
        pos += 4;
        _log.detail("- bitmaps: big="+big+", bidx="+bidx+", scan="+scan+", nglyphs="+ngly);
        if (_metrics != null && _metrics.length != ngly)
            throw new IOException("bitmaps: mismatched glyph count with metrics: "+ngly+"!="+_metrics.length);
        _bitmaps = new PCFBitmap[ngly];
        boolean lsbyte = (fmt & 4) > 0;
        boolean lsbit = (fmt & 8) > 0;
        for (int i = 0; i < ngly; i += 1) {
            int off = getI(is, 4, big);
            pos += 4;
            _bitmaps[i] = new PCFBitmap(1 << bidx, 1 << scan, off, lsbit, lsbyte);
            //log("\t"+i+": 0x"+Integer.toHexString(off));
        }
        int bszs[] = { 0, 0, 0, 0 };
        for (int i = 0; i < 4; i += 1) {
            bszs[i] = getI(is, 4, big);
            pos += 4;
            //og("\tbszs["+i+"]="+bszs[i]);
        }
        _log.detail("\tdata size=0x"+Integer.toHexString(bszs[bidx]));
        _rawbits = new byte[bszs[bidx]];
        fill(is, _rawbits);
        pos += _rawbits.length;
        return pos;
    }
    private int pcfEncodings(InputStream is, int size, int format) throws IOException {
        int fmt = getI(is, 4, false);
        int pos = 4;
        if (fmt != format)
            throw new IOException("encodings: mismatched format: 0x"+Integer.toHexString(fmt)+"!=0x"+Integer.toHexString(format));
        boolean big = (fmt & PCF_BYTE_MASK) > 0;
        int minb2 = getI(is, 2, big);
        int maxb2 = getI(is, 2, big);
        int minb1 = getI(is, 2, big);
        int maxb1 = getI(is, 2, big);
        int dfltC = getI(is, 2, big);   // NB: default char / code point, not glyph..
        pos += 10;
        int nhgh = (maxb1 - minb1 + 1);
        int nlow = (maxb2 - minb2 + 1);
        int[][] map = new int[nhgh][nlow];
        for (int h = 0; h < nhgh; h += 1) {
            for (int l = 0; l < nlow; l += 1) {
                int v = getI(is, 2, big);
                map[h][l] = v < 0xffff ? v : -1;  // adjust 16bit -1 to int
            }
        }
        _encoding = new PCFEncoding(minb1, maxb1, minb2, maxb2, 0, map);
        int dfltG = mapCodePoint(dfltC);
        if (dfltG < 0) {
            _log.error(_name +": encodings: default char does not have a mapping, using glyph 0");
        } else {
            _encoding.dflt = dfltG;
        }
        _log.detail("- encodings: big="+big+", b1="+minb1+"-"+maxb1+", b2="+minb2+"-"+maxb2+" default="+_encoding.dflt);
        pos += 2 * nhgh * nlow;
        return pos;
    }
    private int skip(InputStream is, int size) throws IOException {
        _log.detail("- skipping: 0x"+Integer.toHexString(size));
        for (int i = 0; i < size; i += 1)
            is.read();
        return size;
    }
    private int getI(InputStream is, int cnt, boolean big) throws IOException {
        return (int)getL(is, cnt, big);
    }
    private long getL(InputStream is, int cnt, boolean big) throws IOException {
        long rv = 0;
        byte[] b = new byte[cnt];
        fill(is, b);
        for (int i = 0; i < cnt; i += 1) {
            if (big) {
                rv <<= 8;
                rv |= (int)b[i] & 0xff;
            } else {
                rv <<= 8;
                rv |= (int)b[cnt-i-1] & 0xff;
            }
        }
        return rv;
    }
    private void fill(InputStream is, byte[] b) throws IOException {
        int l = 0;
        while (l < b.length) {
            int n = is.read(b, l, b.length - l);
            if (n < 1)
                throw new IOException("short read");
            l += n;
        }
    }

    // Implementation of Font
    protected String[] findFonts() {
        ArrayList<String> rv = new ArrayList<>();
        // as per the findFont order above - files first, then any resources
        String[] flds = { FONT_PATH+"X11/misc", FONT_PATH+"X11/100dpi" };
        for (var p : getXdgPath().split(":")) {
            for (var s : flds) {
                File t = new File(p, s);
                if (!t.exists())
                    continue;
                for (var f : new File(p, s).listFiles()) {
                    if (f.getName().endsWith(".pcf") || f.getName().endsWith(".pcf.gz")) {
                        rv.add(f.getAbsolutePath());
                        _log.detail("findFonts(disk): "+f.getAbsolutePath());
                    }
                }
            }
        }
        // https://stackoverflow.com/questions/50469600/how-do-you-list-all-files-in-the-resources-folder-java-scala
        try (var fs = FileSystems.newFileSystem(getClass().getResource(FONT_PATH).toURI(), new HashMap<String,String>())) {
            var fp = fs.getPath(FONT_PATH);
            Files.list(fp).filter(p -> p.endsWith(".pcf") || p.endsWith(".pcf.gz")).forEach(
                p -> {
                    rv.add(p.toString());
                    _log.info("findFonts(res): "+p.toString());
                }
            );
        } catch (IOException x) {
            _log.error("findFonts: unable to enumerate resources: "+x.toString());
        } catch (URISyntaxException x) {
            _log.error("findFonts: unable to enumerate resources: "+x.toString());
        }
        return rv.toArray(flds);
    }
    protected String familyName() { return _family; }
    protected int missingGlyph() {
        return _encoding.dflt;
    }
    protected int mapCodePoint(int cp) {
        int h = (cp >> 8) & 0xff;
        int l = cp & 0xff;
        return (h < _encoding.minb1 || h > _encoding.maxb1 || l < _encoding.minb2 || l > _encoding.maxb2) ? -1 : _encoding.map[h-_encoding.minb1][l-_encoding.minb2];
    }
    protected int renderGlyph(RenderContext ctx, int glyph) {
        _log.detail("render glyph:"+glyph+", left="+_metrics[glyph].lbearing+", right="+_metrics[glyph].rbearing+", width="+_metrics[glyph].width+
            ", ascent="+_metrics[glyph].ascent+", descent="+_metrics[glyph].descent);
            glyph = glyph < 0 ? missingGlyph() : glyph;
        // set starting point from origin to top left of bounding box
        int x = _metrics[glyph].lbearing;
        int y = -_metrics[glyph].ascent;
        // calculate glyph pixel size
        int gw = _metrics[glyph].width;
        int gh = _metrics[glyph].ascent + _metrics[glyph].descent;
        // calculate element data sizes
        int ebyts = _bitmaps[glyph].unit;
        int ebits = ebyts * 8;
        // iterate rows
        for (int r = 0; r < gh; r += 1) {
            // point at span
            int o = _bitmaps[glyph].offset + _bitmaps[glyph].span * r;
            // iterate columns along row, fetch bits from elements of unit size, performing bit/byte ordering as defined
            for (int c = 0; c < gw; c += 1) {
                int eo = (c/ebits) * ebyts;
                int eb = _bitmaps[glyph].lsbyte ? (c % ebits) / 8 : ebyts - 1 - (c % ebits) / 8;
                int bo = _bitmaps[glyph].lsbit ? 7 - (c % 8) : c % 8;
                byte b = _rawbits[o + eo + eb];
                if (((b >> bo) & 1) > 0)
                    ctx.setPixel(x + c, y + r);
            }
        }
        return gw;
    }
    protected FontMetrics metrics() { return this; }
    // FontMetrics API
    public Font getFont() { return this; }
    public int getAscent() { return _maxMetrics.ascent; }
    public int getDescent() { return _maxMetrics.descent; }
    public int getHeight() { return _maxMetrics.ascent + _maxMetrics.descent; }
    public int getLeading() { return getHeight(); }
    public int stringWidth(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i += 1)
            w += charWidth(s.codePointAt(i));
        return w;
    }
    public int charWidth(int c) {
        int gl = mapCodePoint(c);
        gl = gl < 0 ? missingGlyph() : gl;
        return _metrics[gl].width;
    }
}
