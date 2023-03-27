package com.ashbysoft.swingland;

public class EmptyBorder implements Border {
    private final Insets _insets;
    public EmptyBorder(int t, int l, int b, int r) { _insets = new Insets(t, l, b, r); }
    public EmptyBorder(Insets i) { _insets = i; }
    public Insets getBorderInsets(Component c) { return _insets; }
    public boolean isBorderOpaque() { return false; }
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {}
}
