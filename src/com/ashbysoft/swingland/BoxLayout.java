package com.ashbysoft.swingland;

// Similar to a flow layout, but does not wrap

public class BoxLayout extends NullLayout {
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;

    private int _axis;
    public BoxLayout(Container target, int axis) {
        _axis = axis;
    }
    public void layoutContainer(Container parent) {
        _log.info("layoutContainer("+parent.getName()+")");
        Insets ins = parent.getInsets();
        int curX = ins._l;
        int curY = ins._t;
        int fixH = parent.getHeight() - ins._t - ins._b;
        int fixW = parent.getWidth() - ins._l - ins._r;
        for (int i = 0; i < parent.getComponentCount(); i += 1) {
            Component c = parent.getComponent(i);
            Dimension d = c.getPreferredSize();
            if (X_AXIS == _axis) {
                c.setBounds(curX, curY, d._w, fixH);
                curX += d._w;
            } else {
                c.setBounds(curX, curY, fixW, d._h);
                curY += d._h;
            }
        }
    }
    public Dimension minimumLayoutSize(Container parent) {
        int maxW = 0;
        int maxH = 0;
        int sum = 0;
        Insets ins = parent.getInsets();
        for (int i = 0; i < parent.getComponentCount(); i += 1) {
            Dimension d = parent.getComponent(i).getPreferredSize();
            maxW = d._w > maxW ? d._w : maxW;
            maxH = d._h > maxH ? d._h : maxH;
            sum += X_AXIS == _axis ? d._w : d._h;
        }
        return X_AXIS == _axis ?
            new Dimension(sum + ins._l + ins._r, maxH + ins._t + ins._b) :
            new Dimension(maxW + ins._l + ins._r, sum + ins._t + ins._b);
    }
}
