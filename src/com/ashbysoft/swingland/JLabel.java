package com.ashbysoft.swingland;

public class JLabel extends JComponent {
    private String _text;
    private int _align;
    public JLabel() { this(""); }
    public JLabel(String text) { this(text, SwingConstants.LEADING); }
    public JLabel(String text, int align) { _text = text; _align = align; }
    public String getText() { return _text; }
    public void setText(String text) { _text = text; refresh(); }
    public int getHorizontalAlignment() { return _align; }
    public int getVerticalAlignment() { return SwingConstants.CENTER; }
    public void setHorizontalAlignment(int align) { if (_align != align) refresh(); _align = align; }
    public void setVerticalAlignment(int align) { throw new IllegalArgumentException("unsupported :("); }
    // prevent use as a container.. for now
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JLabel");
    }
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet())
            return super.getPreferredSize();
        Point p;
        if (_text.length() > 0 && getFont() != null) {
            p = new Point(getFont().getFontMetrics().stringWidth(getText()), getFont().getFontMetrics().getHeight());
        } else {
            // not on screen yet or no text, default to smallish
            p = new Point(0,0);
        }
        // allow 5px space between text and insets
        Insets ins = getInsets();
        return new Dimension(p._x+10+ins._l+ins._r, p._y+10+ins._t+ins._b);
    }
    public Dimension getMinimumSize() { return getPreferredSize(); }
    public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String s = getText();
        if (s.length() > 0) {
            FontMetrics fm = g.getFont().getFontMetrics();
            int w= fm.stringWidth(s);
            int h = fm.getHeight();
            Insets ins = getInsets();
            int x = (SwingConstants.LEFT == _align || SwingConstants.LEADING == _align) ? ins._l+5 :
                (SwingConstants.TRAILING == _align || SwingConstants.RIGHT == _align) ? ins._r-5-w : (getWidth()-w) / 2;    // TODO right-to-left text
            if (isEnabled())
                g.setColor(getForeground());
            else
                g.setColor(Window.DEFAULT_DISABLED);
            g.drawString(s, x, (getHeight()+h)/2);
        }
    }
}
