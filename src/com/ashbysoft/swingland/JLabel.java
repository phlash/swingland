package com.ashbysoft.swingland;

public class JLabel extends JComponent {
    private String _text;
    public JLabel() { this(""); }
    public JLabel(String text) { _text = text; }
    public String getText() { return _text; }
    public void setText(String text) { _text = text; invalidate(); }
    // prevent use as a container.. for now
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JLabel");
    }
    public Dimension getPreferredSize() {
        Point p;
        Graphics g = getGraphics();
        if (g != null && _text.length() > 0) {
            p = new Point(g.getFont().getFontMetrics().stringWidth(getText()), g.getFont().getFontMetrics().getHeight());
        } else {
            // not on screen yet or no text, default to smallish
            p = new Point(0,0);
        }
        return new Dimension(p._x+10, p._y+10);
    }
    public Dimension getMinimumSize() { return getPreferredSize(); }
    public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    public void paint(Graphics g) {
        String s = getText();
        if (s.length() > 0) {
            int h = g.getFont().getFontMetrics().getHeight();
            g.setColor(getForeground());
            g.drawString(s, 5, h+5);
        }
    }
}
