package com.ashbysoft.swingland.image;

public interface ImageConsumer {
    // hints
    public static final int RANDOMPIXELORDER = 1;
    public static final int TOPDOWNLEFTRIGHT = 2;
    public static final int COMPLETESCANLINES = 4;
    public static final int SINGLEPASS = 8;
    public static final int SINGLEFRAME = 16;
    // completion state
    public static final int IMAGEERROR = 1;
    public static final int SINGLEFRAMEDONE = 2;
    public static final int STATICIMAGEDONE = 3;
    public static final int IMAGEABORTED = 4;

    void imageComplete(int status);
    void setDimensions(int w, int h);
    void setHints(int hints);
    void setColorModel(ColorModel m);
    void setPixels(int x, int y, int w, int h, ColorModel m, int[] pixels, int off, int stride);
}
