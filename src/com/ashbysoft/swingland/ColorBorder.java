package com.ashbysoft.swingland;

public class ColorBorder extends EmptyBorder {
    private Color _color;
    public ColorBorder(int t, int l, int b, int r, Color c) { super(t, l, b, r); _color = c; }
    public ColorBorder(Insets i, Color c) { super(i); _color = c; }
    public boolean isBorderOpaque() { return true; }
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Insets insets = getBorderInsets(c);
        g.setColor(_color);
        g.fillRect(x, y, w, insets._t);
        g.fillRect(x, y+h-insets._b, w, insets._b);
        g.fillRect(x, y+insets._t, insets._l, h-insets._t-insets._b);
        g.fillRect(x+w-insets._r, y+insets._t, insets._r, h-insets._t-insets._b);
    }
}
