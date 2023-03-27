package com.ashbysoft.swingland;

public interface Border {
    Insets getBorderInsets(Component c);
    boolean isBorderOpaque();
    void paintBorder(Component c, Graphics g, int x, int y, int w, int h);
}
