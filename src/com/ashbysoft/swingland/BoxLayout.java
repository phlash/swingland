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
        Insets ins = parent.getInsets();
        int curX = ins._l;
        int curY = ins._t;
        for (int i = 0; i < parent.getComponentCount(); i += 1) {
            Component c = parent.getComponent(i);
            Dimension d = c.getPreferredSize();
            c.setBounds(curX, curY, d._w, d._h);
            if (X_AXIS == _axis)
                curX += d._w;
            else
                curY += d._h;
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
