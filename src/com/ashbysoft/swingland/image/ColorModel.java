package com.ashbysoft.swingland.image;

public interface ColorModel {
    int getAlpha(int p);
    int getRed(int p);
    int getGreen(int p);
    int getBlue(int p);
    int getRGB(int p);
    boolean isAlphaPremultiplied();
}
