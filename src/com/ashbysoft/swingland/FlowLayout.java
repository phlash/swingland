package com.ashbysoft.swingland;

// weirdly - not a LayoutManager2
public class FlowLayout implements LayoutManager {
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int LEADING = 3;
    public static final int TRAILING = 4;

    private Logger _log = new Logger("["+getClass().getSimpleName()+"@"+hashCode()+"]:");
    private int _align;
    private int _hgap;
    private int _vgap;
    public FlowLayout() { this(CENTER); }
    public FlowLayout(int align) { this(align, 5, 5); }
    public FlowLayout(int align, int hgap, int vgap) {
        _log.info("<init>("+align+","+hgap+","+vgap+")");
        _align = align;
        _hgap = hgap;
        _vgap = vgap;
    }
    public void addLayoutComponent(String name, Component c) {}
    private Dimension iterate(Container parent, boolean apply) {
        Rectangle bounds = parent.getBounds();
        int curW = 0;
        int curH = 0;
        int maxW = 0;
        int maxH = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Dimension d = c.getPreferredSize();
            // current row?
            int newW = curW + (i > 0 ? _hgap : 0);
            if (newW + d._w <= bounds._w) {
                if (apply) c.setBounds(bounds._x + newW, bounds._y + curH, d._w, d._h);
                curW = newW + d._w;
                maxH = d._h > maxH ? d._h : maxH;
                maxW = curW > maxW ? curW : maxW;
            // wrap to next row
            } else {
                curW = d._w;
                curH += maxH + _vgap;
                maxH = d._h;
                maxW = curW > maxW ? curW : maxW;
                if (apply) c.setBounds(bounds._x, bounds._y + curH, d._w, d._h);
            }
        }
        return new Dimension(maxW, curH + maxH);
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