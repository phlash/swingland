package com.ashbysoft.swingland.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.Image;

public class BufferedImage extends Image implements ImageProducer, ColorModel {
    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;

    private ByteBuffer _buffer;
    private int _width;
    private int _height;
    private int _type;
    private ArrayList<ImageConsumer> _consumers;

    public BufferedImage(int w, int h, int t) {
        // all supported types are 4-bytes per pixel, and we don't have stride > width
        _buffer = ByteBuffer.allocate(w * h * 4);
        _buffer.order(ByteOrder.nativeOrder());
        _width = w;
        _height = h;
        _type = t;
        _consumers = new ArrayList<>();
    }
    // package-private constructor from existing pixel buffer
    BufferedImage(int w, int h, int t, ByteBuffer b) {
        _width = w;
        _height = h;
        _type = t;
        _buffer = b;
        _consumers = new ArrayList<>();
    }
    public Graphics getGraphics() {
        return getGraphics(getBuffer(), getWidth(), getHeight());
    }
    public int getWidth() { return _width; }
    public int getHeight() { return _height; }
    public int getWidth(ImageObserver obs) { return getWidth(); }
    public int getHeight(ImageObserver obs) { return getHeight(); }
    public ImageProducer getSource() { return this; }
    public int getRGB(int x, int y) {
        ByteBuffer b = getBuffer();
        int o = (y * _width + x) * 4;
        if (o < 0 || o > b.limit()-4)
            throw new IllegalArgumentException("co-ordinates out of range: x/y=>o: "+x+"/"+y+"=>"+o);
        return TYPE_INT_ARGB == _type ? b.getInt(o) : b.getInt(o) | 0xff000000;
    }
    public void setRGB(int x, int y, int argb) {
        ByteBuffer b = getBuffer();
        int o = (y * _width + x) * 4;
        if (o < 0 || o > b.limit()-4)
            throw new IllegalArgumentException("co-ordinates out of range: x/y=>o: "+x+"/"+y+"=>"+o);
        b.putInt(o, TYPE_INT_RGB == _type ? argb | 0xff000000 : argb);
    }
    protected ByteBuffer getBuffer() { return _buffer; }

    public void addConsumer(ImageConsumer c) { _consumers.add(c); }
    public boolean isConsumer(ImageConsumer c) { return _consumers.contains(c); }
    public void removeConsumer(ImageConsumer c) { _consumers.remove(c); }
    public void startProduction(ImageConsumer c) {
        ByteBuffer b = getBuffer();
        int[] a = new int[b.limit() / 4];
        for (int i = 0; i < a.length; i += 1)
            a[i] = b.getInt(i * 4);
        addConsumer(c);
        for (ImageConsumer ic : _consumers) {
            ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);
            ic.setDimensions(getWidth(), getHeight());
            ic.setColorModel(this);
            ic.setPixels(0, 0, getWidth(), getHeight(), this, a, 0, getWidth());
            ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
        removeConsumer(c);
        a = null;
    }
    public int getAlpha(int p) { return (p >> 24) & 0xff; }
    public int getRed(int p) { return (p >> 16) & 0xff; }
    public int getGreen(int p) { return (p >> 8) & 0xff; }
    public int getBlue(int p) { return p & 0xff; }
    public int getRGB(int p) { return p; }
    public boolean isAlphaPremultiplied() { return _type == TYPE_INT_ARGB_PRE; }
}
