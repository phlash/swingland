package com.ashbysoft.swingland;

import java.util.LinkedList;

import com.ashbysoft.swingland.geom.AffineTransform;

public class JTabbedPane extends JComponent implements SwingConstants {
    public static final int WRAP_TAB_LAYOUT = 0;
    public static final int SCROLL_TAB_LAYOUT = 1;
    public static final int PAD = 3;

    class Tab {
        String _title;
        Icon _icon;
        Component _comp;
        String _tip;
        Color _fg;
        Color _bg;
        boolean _en;
        Tab(String t, Icon i, Component c, String p) { _title = t; _icon = i; _comp = c; _tip = p; _en = true; }
    }
    private int _selected;
    private int _tabPlace;
    private int _tabLayout;
    private LinkedList<Tab> _tabs;
    private Font _cachedFont;

    public JTabbedPane() { this(SwingConstants.TOP); }
    public JTabbedPane(int tabPlace) { this(tabPlace, WRAP_TAB_LAYOUT); }
    public JTabbedPane(int tabPlace, int tabLayout) {
        _log.info("<init>("+tabPlace+","+tabLayout+")");
        setLayout(new NullLayout());
        setBorder(new ColorBorder(2, 2, 2, 2, Color.BLUE));
        _tabPlace = tabPlace;
        _tabLayout = tabLayout;
        _tabs = new LinkedList<>();
        _selected = -1;
        _cachedFont = null;
    }
    // override add implementation, divert to insertTab()
    protected void addImpl(Component c, Object s, int i) {
        // implement the rules documented by various 'add' methods
        // https://docs.oracle.com/javase/10/docs/api/javax/swing/JTabbedPane.html#add(java.awt.Component)
        String title = (s != null && s instanceof String) ? (String)s : null;
        Icon icon = (s != null && s instanceof Icon) ? (Icon)s : null;
        if (null == title && null == icon)
            title = c.getName();
        // fixup insert position
        if (i < 0)
            i = getTabCount();
        insertTab(title, icon, c, null, i);
    }
    public void addTab(String title, Component comp) { addTab(title, null, comp, null); }
    public void addTab(String title, Icon icon, Component comp) { addTab(title, icon, comp, null); }
    public void addTab(String title, Icon icon, Component comp, String tip) {
        insertTab(title, icon, comp, tip, getTabCount());
    }
    public void insertTab(String title, Icon icon, Component comp, String tip, int i) {
        _log.info("insertTab("+title+","+icon+","+comp.getName()+","+tip+","+i+")");
        _tabs.add(i, new Tab(title, icon, comp, tip));
        if (_selected < 0)
            setSelectedIndex(0);
        invalidate();
    }
    // override remove operations
    public void remove(Component c) { remove(indexOfComponent(c)); }
    public void removeTabAt(int i) { remove(i); }
    public void remove(int i) {
        if (i >= 0 && i < getTabCount()) {
            _log.info("remove("+i+")");
            _tabs.remove(i);
            // adjust selected if we took away the tab at or below that position
            int s = _selected >= i ? _selected >= getTabCount() ? _selected - 1 : _selected : _selected;
            if (s != _selected)
                setSelectedIndex(s);
            invalidate();
        }
    }
    public void removeAll() {
        _log.info("removeAll()");
        _tabs.clear();
        _selected = -1;
        invalidate();
    }
    // general find and manipulate methods..
    public int indexOfComponent(Component c) {
        for (int i = 0; i < getTabCount(); i += 1)
            if (_tabs.get(i)._comp.equals(c))
                return i;
        return -1;
    }
    public int indexOfTab(String t) {
        for (int i = 0; i < getTabCount(); i += 1)
            if (_tabs.get(i)._title != null && _tabs.get(i)._title.equals(t))
                return i;
        return -1;
    }
    public int indexOfTab(Icon n) {
        for (int i = 0; i < getTabCount(); i += 1)
            if (_tabs.get(i)._icon != null && _tabs.get(i)._icon.equals(n))
                return i;
        return -1;
    }
    public Component getSelectedComponent() { return _selected < 0 ? null : getComponentAt(_selected); }
    public void setSelectedComponent(Component c) { setSelectedIndex(indexOfComponent(c)); }
    public int getSelectedIndex() { return _selected; }
    public void setSelectedIndex(int i) {
        if (i != _selected && i >= 0 && i < getTabCount()) {
            _log.info("setSelectedIndex("+i+")");
            _selected = i;
            super.remove(0);
            super.addImpl(getSelectedComponent(), null, -1);
            invalidate();
        }
    }
    public int getTabPlacement() { return _tabPlace; }
    public void setTabPlacement(int p) { _tabPlace = p; invalidate(); }
    public int getTabLayoutPolicy() { return _tabLayout; }
    public void setTabLayoutPolicy(int p) { _tabLayout = p; invalidate(); }
    public int getTabCount() { return _tabs.size(); }
    public Color getBackgroundAt(int i) { return _tabs.get(i)._bg; }
    public void setBackgroundAt(int i, Color c) { _tabs.get(i)._bg = c; checkRepaint(i); }
    public Color getForegrondAt(int i) { return _tabs.get(i)._fg; }
    public void setForegroundAt(int i, Color c) { _tabs.get(i)._fg = c; checkRepaint(i); }
    public Component getComponentAt(int i) { return _tabs.get(i)._comp; }
    public void setComponentAt(int i, Component c) { _tabs.get(i)._comp = c; checkRepaint(i); }
    public Icon getIconAt(int i) { return _tabs.get(i)._icon; }
    public void setIconAt(int i, Icon n) { _tabs.get(i)._icon = n; checkRepaint(i); }
    public String getTitleAt(int i) { return _tabs.get(i)._title; }
    public void setTitleAt(int i, String t) { _tabs.get(i)._title = t; checkRepaint(i); }
    public String getToolTipTextAt(int i) { return _tabs.get(i)._tip; }
    public void setToopTipTextAt(int i, String t) { _tabs.get(i)._tip = t; checkRepaint(i); }
    public boolean isEnabledAt(int i) { return _tabs.get(i)._en; }
    public void setEnabledAt(int i, boolean e) { _tabs.get(i)._en = e; checkRepaint(i); }
    private void checkRepaint(int i) { if (_selected == i) repaint(); }

    // minimum size is just big enough for selected tab component + tabs themselves
    // TODO: _tabLayout support
    public Dimension getPreferredSize() { return getMinimumSize(); }
    public Dimension getMinimumSize() {
        Insets ins = getInsets();
        Dimension sd = getSelectedIndex() < 0 ? null : getSelectedComponent().getMinimumSize();
        int w = sd != null ? sd._w + ins._l + ins._r : ins._l + ins._r;
        int h = sd != null ? sd._h + ins._t + ins._b : ins._t + ins._b;
        boolean isw = SwingConstants.TOP == getTabPlacement() || SwingConstants.BOTTOM == getTabPlacement();
        FontMetrics fm = getFont().getFontMetrics();
        int tl = 0;
        for (var t : _tabs) {
            tl += t._title != null ? fm.stringWidth(t._title) : 0;
            tl += t._icon != null ? isw ? t._icon.getIconWidth() : t._icon.getIconHeight() : 0;
            tl += 2 * PAD;
        }
        if (isw) {
            w = Math.max(w, tl);
            h += fm.getHeight() + 2 * PAD;
        } else {
            w = fm.getHeight() + 2 * PAD;
            h = Math.max(h, tl);
        }
        return new Dimension(w, h);
    }
    // override normal layout manager interaction
    protected boolean isValidateRoot() { return true; }
    protected void validateTree() {
        // iterate all tabs, ensure selected component is visible, others not
        for (int i = 0; i < getTabCount(); i += 1)
            getComponentAt(i).setVisible(getSelectedIndex() == i);
        // size selected component & validate (if any)
        Component s = getSelectedComponent();
        if (s != null) {
            Insets ins = getInsets();
            int fh = getFont().getFontMetrics().getHeight();
            switch (getTabPlacement()) {
                case SwingConstants.TOP -> s.setBounds(ins._l, ins._t + fh + 2 * PAD, getWidth() - ins._l - ins._r, getHeight() - fh - 2 * PAD - ins._t - ins._b);
                case SwingConstants.BOTTOM -> s.setBounds(ins._l, ins._t, getWidth() - ins._l - ins._r, getHeight() - fh - 2 * PAD - ins._t - ins._b);
                case SwingConstants.LEFT -> s.setBounds(ins._l + fh + 2 * PAD, ins._t, getWidth() - fh - 2 * PAD - ins._l - ins._r, getHeight() - ins._t - ins._b);
                case SwingConstants.RIGHT -> s.setBounds(ins._l, ins._t, getWidth() - fh - 2 * PAD - ins._l - ins._r, getHeight() - ins._t - ins._b);
                default -> throw new IllegalArgumentException("tab placement invalid: "+getTabPlacement());
            }
            s.validate();
        }
        // drop our cached font (if any)
        _cachedFont = null;
    }
    // paint the tabs
    protected void paintComponent(Graphics g) {
        // cache font, possibly rotated
        boolean rot = SwingConstants.LEFT == getTabPlacement() || SwingConstants.RIGHT == getTabPlacement();
        if (null == _cachedFont) {
            if (rot) {
                AffineTransform tr = new AffineTransform();
                tr.rotate(-0.5 * Math.PI);
                _cachedFont = getFont().deriveFont(tr);
            } else {
                _cachedFont = getFont();
            }
        }
        Font old = g.getFont();
        g.setFont(_cachedFont);
        FontMetrics fm = _cachedFont.getFontMetrics();
        Insets ins = getInsets();
        int p = 0;
        int th = fm.getHeight() + 2 * PAD;
        for (var t : _tabs) {
            int tl = t._title != null ? fm.stringWidth(t._title) : 0;
            tl += t._icon != null ? rot ? t._icon.getIconHeight() : t._icon.getIconWidth() : 0;
            tl += 2 * PAD;
            switch (getTabPlacement()) {
                case SwingConstants.TOP -> p += paintHTab(g, t, p + ins._l, ins._t, tl, th);
                case SwingConstants.BOTTOM -> p += paintHTab(g, t, p + ins._l, getHeight() - ins._b - th, tl, th);
                case SwingConstants.LEFT -> p += paintVTab(g, t, ins._l, getHeight() - ins._b - p, th, tl);
                case SwingConstants.RIGHT -> p += paintVTab(g, t, getWidth() - ins._r - th, getHeight() - ins._b - p, th, tl);
                default -> {}
            }
        }
        g.setFont(old);
    }
    private int paintHTab(Graphics g, Tab t, int x, int y, int w, int h) {
        g.setColor(t._bg != null ? t._bg : getBackground());
        g.fillRect(x, y, w, h);
        g.setColor(t._fg != null ? t._fg : getForeground());
        // selected tab - wider border
        if (t._comp.isVisible())
            g.drawRect(x+1, y+1, w-3, h-3);
        g.drawRect(x, y, w-1, h-1);
        g.drawString(t._title, x + PAD, y + h - PAD);
        return w;
    }
    private int paintVTab(Graphics g, Tab t, int x, int y, int w, int h) {
        g.setColor(t._bg != null ? t._bg : getBackground());
        g.fillRect(x, y-h, w, h);
        g.setColor(t._fg != null ? t._fg : getForeground());
        // selected tab - wider border
        if (t._comp.isVisible())
            g.drawRect(x+1, y-h+1, w-3, h-3);
        g.drawRect(x, y-h, w-1, h-1);
        g.drawString(t._title, x + w - PAD, y - PAD);
        return h;
    }
}
