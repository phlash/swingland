package com.ashbysoft.test;

// Based on specification: https://fontforge.org/docs/techref/pcf-format.html
// with considerable help from: https://github.com/tdm/android-x-server/blob/master/src/tdm/xserver/FontDataPCF.java

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class PCFTest {
    // tables types
    public static final int PCF_PROPERTIES = (1 << 0);
    public static final int PCF_METRICS = (1 << 2);
    public static final int PCF_BITMAPS = (1 << 3);
    public static final int PCF_BDF_ENCODINGS = (1 << 5);
    // table format bits
    public static final int PCF_BYTE_MASK = (1 << 2);   // indicates big endian
    public static final int PCF_COMPRESSED_METRICS = (1 << 8);
    public static final int PCF_GLYPH_PAD_MASK = (3 << 0);
    public static final int PCF_SCAN_UNIT_MASK = (3 << 4);

    public static void main(String[] args) {
        var us = new PCFTest();
        us.run(args);    
    }
    private void log(String m) {
        System.out.println(m);
    }
    private void run(String[] args) {
        for (var a: args) {
            log("Loading: "+a);
            try (var is = new BufferedInputStream(a.endsWith(".gz") ? new GZIPInputStream(new FileInputStream(a)) : new FileInputStream(a))) {
                load(is);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

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
    // PCF font metrics
    class PCFMetrics {
        public int lbearing, rbearing, width, ascent, descent, attributes;
        public PCFMetrics(int l, int r, int w, int a, int d, int t) { lbearing = l; rbearing = r; width = w; ascent = a; descent = d; attributes = t; }
    }
    private PCFMetrics[] _metrics = null;
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

    private void load(InputStream is) throws IOException {
        int pos = 0;
        // header
        int id = getI(is, 4, false);
        if (id != 0x70636601)   // 'pcf.'
            throw new IOException("Not a PCF font file");
        int tcnt = getI(is, 4, false);
        pos += 8;
        log("- tables: "+tcnt);
        int max = 0;
        for (int i = 0; i < tcnt; i += 1) {
            int t = getI(is, 4, false);
            int f = getI(is, 4, false);
            int s = getI(is, 4, false);
            int o = getI(is, 4, false);
            pos += 16;
            log("\ttype:0x"+Integer.toHexString(t)+", format:0x"+Integer.toHexString(f)+", size:0x"+Integer.toHexString(s)+", offset:0x"+Integer.toHexString(o));
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
            log("- @0x"+Integer.toHexString(pos));
            switch (te.type) {
                case PCF_PROPERTIES -> pos += pcfProperties(is, te.size, te.format);
                case PCF_METRICS -> pos += pcfMetrics(is, te.size, te.format);
                case PCF_BITMAPS -> pos += pcfBitmaps(is, te.size, te.format);
                case PCF_BDF_ENCODINGS -> pos += pcfEncodings(is, te.size, te.format);
                default -> pos += skip(is, te.size);
            }
        }
        // ASCII Art dump of some random encodings
        int r2 = _encoding.maxb2 - _encoding.minb2 + 1;
        Random rnd = new Random();
        for (int i = 0; i < 10; i+= 1)
            pcfDump((_encoding.minb1 << 8) | ((rnd.nextInt(r2) + _encoding.minb2)));
    }
    private void pcfDump(int enc) throws IOException {
        // map to glyph
        int b1 = (enc >> 8) & 0xff;
        int b2 = enc & 0xff;
        if (b1 < _encoding.minb1 || b1 > _encoding.maxb1 || b2 < _encoding.minb2 || b2 > _encoding.maxb2)
            throw new IOException("encoding out of range: 0x"+Integer.toHexString(enc));
        int glyph = _encoding.map[b1 - _encoding.minb1][b2 - _encoding.minb2];
        // check for default
        if (0xffff == glyph)
            glyph = _encoding.dflt;
        log("-- enc: 0x"+Integer.toHexString(enc)+", glyph:"+glyph+", left="+_metrics[glyph].lbearing+", right="+_metrics[glyph].rbearing+", width="+_metrics[glyph].width+
            ", ascent="+_metrics[glyph].ascent+", descent="+_metrics[glyph].descent);
        // calculate glyph pixel size
        int gw = _metrics[glyph].width;
        int gh = _metrics[glyph].ascent + _metrics[glyph].descent;
        char[] ascii = new char[gw];
        // calculate element data sizes
        int ebyts = _bitmaps[glyph].unit;
        int ebits = ebyts * 8;
        // iterate rows
        for (int y = 0; y < gh; y += 1) {
            // point at span
            int o = _bitmaps[glyph].offset + _bitmaps[glyph].span * y;
            // iterate pixels along row, fetch bits from elements of unit size, performing bit/byte ordering as defined
            for (int x = 0; x < gw; x += 1) {
                int eo = (x/ebits) * ebyts;
                int eb = _bitmaps[glyph].lsbyte ? (x % ebits) / 8 : ebyts - 1 - (x % ebits) / 8;
                int bo = _bitmaps[glyph].lsbit ? 7 - (x % 8) : x % 8;
                byte b = _rawbits[o + eo + eb];
                ascii[x] = ((b >> bo) & 1) > 0 ? 'x' : '.';
            }
            log(new String(ascii));
        }
    }
    private int pcfProperties(InputStream is, int size, int format) throws IOException {
        int fmt = getI(is, 4, false);
        int pos = 4;
        boolean big = (fmt & PCF_BYTE_MASK) > 0;     // PCF_BYTE_MASK
        if (fmt != format)
            throw new IOException("props: mismatched format: 0x"+Integer.toHexString(fmt)+"!=0x"+Integer.toHexString(format));
        int nprp = getI(is, 4, big);
        pos += 4;
        log("- props: big="+big+", nprops="+nprp);
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
        for (var p : _props.values())
            log("\t"+_pstrs.get(p.offset)+"="+ (p.isString ? "\""+_pstrs.get(p.value)+"\"" : "0x"+Integer.toHexString(p.value)));
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
        log("- metrics: big="+big+", comp="+comp+", nmetrics="+nmet);
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
            //log("\t"+i+": lbrg="+lbrg+" rbrg="+rbrg+" cwid="+cwid+" casc="+casc+" cdsc="+cdsc+" attr=0x"+Integer.toHexString(attr));
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
        log("- bitmaps: big="+big+", bidx="+bidx+", scan="+scan+", nglyphs="+ngly);
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
        log("\tdata size=0x"+Integer.toHexString(bszs[bidx]));
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
        int defaultGlyph = getI(is, 2, big);
        pos += 10;
        int nhgh = (maxb1 - minb1 + 1);
        int nlow = (maxb2 - minb2 + 1);
        log("- encodings: big="+big+", b1="+minb1+"-"+maxb1+", b2="+minb2+"-"+maxb2+" default="+defaultGlyph);
        _encoding = new PCFEncoding(minb1, maxb1, minb2, maxb2, defaultGlyph, new int[nhgh][nlow]);
        for (int h = 0; h < nhgh; h += 1)
            for (int l = 0; l < nlow; l += 1)
                _encoding.map[h][l] = getI(is, 2, big);
        pos += 2 * nhgh * nlow;
        return pos;
    }
    private int skip(InputStream is, int size) throws IOException {
        log("- skipping: 0x"+Integer.toHexString(size));
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
}
