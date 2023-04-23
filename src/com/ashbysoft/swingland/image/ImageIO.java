package com.ashbysoft.swingland.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.ashbysoft.logger.Logger;

public class ImageIO {
    private static final Logger _log = new Logger("[ImageIO]:");
    private static String _short = "Short read";

    public static BufferedImage read(byte[] data) throws IOException {
        _log.info("read(byte[])");
        return read(new ByteArrayInputStream(data));
    }
    public static BufferedImage read(String pathname) throws IOException {
        _log.info("read("+pathname+")");
        return read(new File(pathname));
    }
    public static BufferedImage read(File file) throws IOException {
        _log.info("read(File:"+file.toString()+")");
        if (!file.canRead())
            throw new IOException("unreadable: "+file.getCanonicalPath());
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return read(is);
        }
    }
    public static BufferedImage read(URL url) throws IOException {
        _log.info("read(URL:"+url.toString()+")");
        try (InputStream is = url.openStream()) {
            return read(is);
        }
    }
    public static BufferedImage read(InputStream is) throws IOException {
        _log.info("read(Stream)");
        // let's find out what we have..
        byte[] id = { 0, 0, 0, 0 };
        fill (is, id, "@ID");
        int[] wht = { 0, 0, 0 };
        ByteBuffer b =
        // QOI?
        ('q' == id[0] && 'o' == id[1] && 'i' == id[2] && 'f' == id[3]) ? readQOI(is, id, wht)
        // PNG?
        : (0x89 == (id[0] & 0xff) && 'P' == id[1] && 'N' == id[2] && 'G' == id[3]) ? readPNG(is, id, wht)
        // BMP?
        : ('B' == id[0] && 'M' == id[1]) ? readBMP(is, id, wht)
        // Nope out
        : null;
        return b != null ? new BufferedImage(wht[0], wht[1], wht[2], b) : null;
    }

    // @see https://qoiformat.org/
    private static ByteBuffer readQOI(InputStream is, byte[] id, int[] wht) throws IOException {
        _log.info("readQOI()");
        // read remainder of header
        int w = (int)readUval(is, 32, true);
        int h = (int)readUval(is, 32, true);
        int ch = (byte)readUval(is, 8, true);
        int cs = (byte)readUval(is, 8, true);
        _log.detail("- w="+w+",h="+h+",ch="+ch+",cs="+cs);
        ByteBuffer b = ByteBuffer.allocate(w * h * 4);
        b.order(ByteOrder.nativeOrder());
        int[] hist = new int[64];
        int[] lrgba = { 0, 0, 0, 255 };
        int largb = 0xff000000;
        int p = 0;
        while (p < w * h) {
            int tag = is.read();
            if (tag < 0) throw new IOException(_short + "@tag/"+p);
            if (0xFE == tag) {
                // RGB values
                byte[] rgb = { 0, 0, 0 };
                fill(is, rgb, "@rgb/"+p);
                lrgba[0] = rgb[0] & 0xff;
                lrgba[1] = rgb[1] & 0xff;
                lrgba[2] = rgb[2] & 0xff;
            } else if (0xFF == tag) {
                // RGBA values
                byte[] rgba = { 0, 0, 0, 0 };
                fill(is, rgba, "@rgba/"+p);
                lrgba[0] = rgba[0] & 0xff;
                lrgba[1] = rgba[1] & 0xff;
                lrgba[2] = rgba[2] & 0xff;
                lrgba[3] = rgba[3] & 0xff;
            } else if (0x00 == (tag & 0xC0)) {
                // INDEX
                int hi = tag & 0x3F;
                largb = hist[hi];
                lrgba[0] = (largb >> 16) & 0xff;
                lrgba[1] = (largb >> 8) & 0xff;
                lrgba[2] = (largb) & 0xff;
                lrgba[3] = (largb >> 24) & 0xff;
            } else if (0x40 == (tag & 0xC0)) {
                // DIFF
                int dr = ((tag & 0x30) >> 4) - 2;
                int dg = ((tag & 0x0C) >> 2) - 2;
                int db = (tag & 0x3) - 2;
                lrgba[0] = (lrgba[0] + dr) & 0xff;
                lrgba[1] = (lrgba[1] + dg) & 0xff;
                lrgba[2] = (lrgba[2] + db) & 0xff;
            } else if (0x80 == (tag & 0xC0)) {
                // LUMA
                int dg = (tag & 0x3f) - 32;
                int drb = is.read();
                if (drb < 0) throw new IOException(_short+"@/luma"+p);
                int dr = (drb >> 4) - 8 + dg;
                int db = (drb & 0xF) - 8 + dg;
                lrgba[0] = (lrgba[0] + dr) & 0xff;
                lrgba[1] = (lrgba[1] + dg) & 0xff;
                lrgba[2] = (lrgba[2] + db) & 0xff;
            } else if (0xC0 == (tag & 0xC0)) {
                // RUN
                int run = (tag & 0x3f) + 1;
                while (run > 0) {
                    b.putInt(largb);
                    p += 1;
                    run -= 1;
                }
                continue;
            }
            // save to history, add to buffer
            largb = (lrgba[3] << 24) | (lrgba[0] << 16) | (lrgba[1] << 8) | (lrgba[2]);
            int hi = (lrgba[0]*3 + lrgba[1]*5 + lrgba[2]*7 + lrgba[3]*11) % 64;
            hist[hi] = largb;
            b.putInt(largb);
            p += 1;
        }
        wht[0] = w;
        wht[1] = h;
        wht[2] = ch > 3 ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        return b;
    }

    // @see https://www.w3.org/TR/png/
    private static ByteBuffer readPNG(InputStream is, byte[] id, int[] wht) throws IOException {
        _log.info("readPNG()");
        readUval(is, 32, true); // skip rest of ID header (CR-LF and ETX markers)
        // read 1st chunk, must be IHDR
        int clen = (int)readUval(is, 32, true);
        int ctyp = (int)readUval(is, 32, true);
        if (clen != 13 || ctyp != 0x49484452)   // 'IHDR'
            throw new IOException("invalid IHDR chunk");
        int w = (int)readUval(is, 32, true);
        int h = (int)readUval(is, 32, true);
        int bd = is.read();
        int ct = is.read();
        int cm = is.read();
        int fm = is.read();
        int im = is.read();
        readUval(is, 32, true); // skip CRC-32
        if (bd < 0 || ct < 0 || cm < 0 || fm < 0 || im < 0)
            throw new IOException("short read in IHDR");
        _log.detail("- w="+w+",h="+h+",bd="+bd+",ct="+ct+",cm="+cm+",fm="+fm+",im="+im);
        // dump all unsupported variations
        if (bd != 8 || (ct != 2 && ct != 6) || im != 0)
            throw new IOException("unsupported format, must have: bit depth 8, colour type truecolour (optional alpha), non-interlaced - sorry..");
        ByteBuffer b = ByteBuffer.allocate(w *  h * 4);
        b.order(ByteOrder.nativeOrder());
        Inflater inf = new Inflater();
        int bpp = (6 == ct ? 4 : 3);
        // each row/line/scanline in filtered data starts with a type byte, hence we add one to scan line length
        int bps = w * bpp + 1;
        byte[] fdat = new byte[h * bps];
        int p = 0;
        while (p < fdat.length) {
            clen = (int)readUval(is, 32, true);
            ctyp = (int)readUval(is, 32, true);
            // got an IDAT?
            if (0x49444154 == ctyp) {   // 'IDAT'
                // process it..
                byte[] cdat = new byte[clen];
                fill(is, cdat, "@fdat/"+p);
                inf.setInput(cdat);
                while (!inf.needsInput()) {
                    byte[] pdat = { 0 };
                    try {
                        if (inf.inflate(pdat) > 0) {
                            fdat[p] = pdat[0];
                            p += 1;
                        }
                    } catch (DataFormatException e) {
                        throw new IOException(e);
                    }
                }
            } else {
                // skip it
                if (is.skip(clen) != clen)
                    throw new IOException("failed to skip chunk, clen="+clen+", ctyp="+Integer.toHexString(ctyp));
            }
            readUval(is, 32, true); // skip CRC-32
        }
        if (!inf.finished())
            throw new IOException("not at end of compressed data");
        inf.end();
        // fdat now contains filtered image data.. we reconstruct into the final image
        // these buffers have an additional pixel 'on the left' to provide the zero value
        // at the start of each line
        byte[] pline = new byte[(w+1) * bpp];
        byte[] cline = new byte[(w+1) * bpp];
        for (int y = 0; y < h; y += 1) {
            // grab filter type byte
            int ft = (int)fdat[y * bps];
            for (int x = 0; x < w; x += 1) {
                for (int c = 0; c < bpp; c += 1) {
                    // x & y are pixel indicies, c is sub-pixel index, +1 for filter type byte
                    int px = fdat[y * bps + x * bpp + c + 1] & 0xff;
                    int pa = cline[x * bpp + c] & 0xff;
                    int pb = pline[(x+1) * bpp + c] & 0xff;
                    int pc = pline[x * bpp + c] & 0xff;
                    int rx = (0 == ft) ? px             // filter type 0: no action
                        : (1 == ft) ? pa + px           // filter type 1: diff from pixel before
                        : (2 == ft) ? pb + px           // filter type 2: diff from line above
                        : (3 == ft) ? (pa + pb)/2 + px  // filter type 3: diff from average of both
                        : (4 == ft) ? predict(pa, pb, pc) + px  // filter type 4: Paeth predictor diff
                        : -1;
                    cline[(x+1) * bpp + c] = (byte)rx;
                }
                // build ARGB pixel and stuff in b
                int red = (int)cline[(x+1) * bpp + 0] & 0xff;
                int grn = (int)cline[(x+1) * bpp + 1] & 0xff;
                int blu = (int)cline[(x+1) * bpp + 2] & 0xff;
                int alp = (bpp > 3) ? (int)cline[(x+1) * bpp + 3] & 0xff : 0xff;
                int argb = (alp << 24) | (red << 16) | (grn << 8) | blu;
                b.putInt(argb);
            }
            // swap prev and current lines
            byte[] t = pline;
            pline = cline;
            cline = t;
        }
        wht[0] = w;
        wht[1] = h;
        wht[2] = bpp > 3 ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        return b;
    }
    private static int predict(int a, int b, int c) {
        int p = a + b - c;
        int pa = p - a; if (pa < 0) pa = -pa;
        int pb = p - b; if (pb < 0) pb = -pb;
        int pc = p - c; if (pc < 0) pc = -pc;
        if (pa <= pb && pa <= pc) return a;
        else if (pb <= pc) return b;
        return c;
    }
    private static ByteBuffer readBMP(InputStream is, byte[] id, int[] wht) {
        return null;
    }
    private static int fill(InputStream is, byte[] b, String e) throws IOException {
        int r = 0;
        while (r < b.length) {
            int n = is.read(b, r, b.length-r);
            if (n <= 0) throw new IOException(_short+e);
            r += n;
        }
        return r;
    }
    // read a given number of bits (8/16/32/64) in a given format (big/little endian)
    private static long readUval(InputStream is, int bits, boolean big) throws IOException {
        int nbytes = (bits)/8;
        byte[] ba = new byte[nbytes];
        fill(is, ba, "");
        long rv = 0;
        for (int b = 0; b < nbytes; b += 1) {
            int o = big ? b : nbytes - 1 - b;
            rv <<= 8;
            rv |= (long)ba[o] & 0xff;
        }
        return rv;
    }
}
