package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;

// weirdly - not a LayoutManager2
public class FlowLayout implements LayoutManager {
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int LEADING = 3;
    public static final int TRAILING = 4;

    private Logger _log = new Logger("["+getClass().getSimpleName()+"@"+hashCode()+"]:");
    private int _hgap;
    private int _vgap;
    public FlowLayout() { this(LEFT); }
    public FlowLayout(int align) { this(align, 5, 5); }
    public FlowLayout(int align, int hgap, int vgap) {
        _log.info("<init>("+align+","+hgap+","+vgap+")");
        if (LEFT != align)
            _log.error("warning: FlowLayout only supports LEFT align at the moment..");
        _hgap = hgap;
        _vgap = vgap;
    }
    public void addLayoutComponent(String name, Component c) {}
    private Dimension iterate(Container parent, boolean apply) {
        Rectangle bounds = parent.getBounds();
        Insets insets = parent.getInsets();
        int curW = insets._l;
        int curH = insets._t;
        int insW = bounds._w - insets._l - insets._r;
        int maxW = 0;
        int maxH = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Dimension d = c.getPreferredSize();
            // current row?
            int newW = curW + (i > 0 ? _hgap : 0);
            if (!apply || newW + d._w <= insW) {
                if (apply) c.setBounds(newW, curH, d._w, d._h);
                curW = newW + d._w;
                maxH = d._h > maxH ? d._h : maxH;
                maxW = curW > maxW ? curW : maxW;
            // wrap to next row
            } else {
                curW = insets._l + d._w;
                curH += maxH + _vgap;
                maxH = d._h;
                maxW = curW > maxW ? curW : maxW;
                if (apply) c.setBounds(insets._l, insets._t + curH, d._w, d._h);
            }
        }
        return new Dimension(maxW + insets._r, curH + maxH + insets._b);
    }
    public void layoutContainer(Container parent) {
        // wrap components into parent width, row-by-row
        _log.info("layoutContainer("+parent.getName()+")");
        iterate(parent, true);
    }
    public Dimension minimumLayoutSize(Container parent) {
        return iterate(parent, false);
    }
    public Dimension preferredLayoutSize(Container parent) { return minimumLayoutSize(parent); }
    public void removeLayoutComponent(Component c) {}
}