package com.ashbysoft.swingland;

// horizontal menu bar to fit into a JRootPane

public class JMenuBar extends JComponent {
    public static final int BORDER_WIDTH =2;
    private boolean _paintBorder;
    private boolean _hasHelp;
    public JMenuBar() {
        _log.info("<init>()");
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMargin(new Insets(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        setBorderPainted(true);
        add(new GlueComponent());
        _hasHelp = false;
    }
    public int getMenuCount() { return getComponentCount() - 1; }
    public JMenu getMenu(int i) {
        // skip over glue
        if (_hasHelp && getMenuCount() - 1 == i)
            i += 1;
        return (JMenu)getComponent(i);
    }
    public void add(JMenu item) {
        _log.info("add("+item.getText()+")");
        // insert at glue position, pushing glue and possibly help item up
        add(item, _hasHelp ? getMenuCount()-1 : getMenuCount());
    }
    public void remove(int i) {
        // skip over the glue
        if (_hasHelp && getMenuCount() - 1 == i)
            i += 1;
        super.remove(i);
    }
    // special consideration for help menu
    public JMenu getHelpMenu() {
        if (_hasHelp)
            return (JMenu)getComponent(getMenuCount());
        return null;
    }
    public void setHelpMenu(JMenu item) {
        _log.info("setHelpMenu("+item.getText()+")");
        if (_hasHelp) {
            remove(getMenuCount());
            _hasHelp = false;
        }
        if (item != null) {
            // add above glue
            add(item, -1);
            _hasHelp = true;
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
}
