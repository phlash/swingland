package com.ashbysoft.swingland;

// We use our BorderLayout (default) to separate main menu items from help menu
// we use a sub-JPanel with a FlowLayout (default) for main items

public class JMenuBar extends JComponent {
    public static final int BORDER_WIDTH =2;
    private JPanel _main;
    private JMenu _help;
    private boolean _paintBorder;
    public JMenuBar() {
        _log.info("<init>()");
        _main = new JPanel();
        super.addImpl(_main, BorderLayout.CENTER, -1);
        setMargin(new Insets(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));    // random ;-)
        setBorderPainted(true);
    }
    // b0rk attempts to use the undelying container
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components directly to a JMenuBar");
    }
    // forward item add/remove to underlying JPanel
    public int getMenuCount() { return _main.getComponentCount(); }
    public JMenu getMenu(int i) {
        if (i >= 0 && i < getMenuCount())
            return (JMenu)_main.getComponent(i);
        return null;
    }
    public void add(JMenu item) {
        _log.info("add("+item.getText()+")");
        _main.add(item);
    }
    public void remove(int i) { _main.remove(i); }
    public void remove(Component c) { _main.remove(c); }
    // special consideration for help menu
    public JMenu getHelpMenu() { return _help; }
    public void setHelpMenu(JMenu item) {
        _log.info("setHelpMenu("+item.getText()+")");
        if (_help != null) {
            super.remove(_help);
            _help = null;
        }
        if (item != null) {
            _help = item;
            super.addImpl(_help, BorderLayout.EAST, -1);
        }
    }
    public Insets getMargin() { return getInsets(); }
    public void setMargin(Insets m) { setInsets(m); }
    public boolean isBorderPainted() { return _paintBorder; }
    public void setBorderPainted(boolean b) {
        if (b != _paintBorder) {
            _paintBorder = b;
            invalidate();
        }
    }
    public void paintBorder(Graphics g) {
        if (isBorderPainted()) {
            g.setColor(getForeground());
            Insets m = getMargin();
            g.fillRect(0, 0, getWidth(), m._t);
            g.fillRect(0, m._t, m._l, getHeight()-m._b);
            g.fillRect(getWidth()-m._r, m._t, m._r, getHeight()-m._b);
            g.fillRect(0, getHeight()-m._b, getWidth(), m._b);
        }
    }
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
