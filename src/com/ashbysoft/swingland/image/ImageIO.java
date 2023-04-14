package com.ashbysoft.swingland.image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.ashbysoft.logger.Logger;

public class ImageIO {
    private static final Logger _log = new Logger("[ImageIO]:");
    private static String _short = "Short read";

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
    public static BufferedImage read(InputStream is) throws IOException {
        _log.info("read(Stream)");
        // let's find out what we have..
        byte[] id = { 0, 0, 0, 0 };
        if (is.read(id) != id.length)
            throw new IOException(_short + "@ID");
        int[] wht = { 0, 0, 0 };
        ByteBuffer b =
        // QOI?
        ('q' == id[0] && 'o' == id[1] && 'i' == id[2] && 'f' == id[3]) ? readQOI(is, id, wht)
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
