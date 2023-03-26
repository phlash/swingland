package com.ashbysoft.swingland;

public interface FontMetrics {
    Font getFont();
    int getAscent();
    int getDescent();
    int getHeight();
    int getLeading();
    int stringWidth(String s);
}
