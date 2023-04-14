package com.ashbysoft.swingland;

public class BorderLayout extends NullLayout {
    public static final String NORTH = "NORTH";
    public static final String SOUTH = "SOUTH";
    public static final String WEST = "WEST";
    public static final String EAST = "EAST";
    public static final String CENTER = "CENTER";

    // only one occupant of each position, so we know the maximum
    private Component[] _components = new Component[5];
    public void addLayoutComponent(Component c, Object s) {
        String t = s!=null ? s.toString() : CENTER; // default center
        int i;
        if (NORTH.equals(t))
            i = 0;
        else if (SOUTH.equals(t))
            i = 1;
        else if (WEST.equals(t))
            i = 2;
        else if (EAST.equals(t))
            i = 3;
        else if (CENTER.equals(t))
            i = 4;
        else
            throw new IllegalArgumentException("Invalid BorderLayout constraint: "+t);
        if (_components[i]!=null)
            throw new IllegalArgumentException("BorderLayout already contains a component at: "+t);
        _components[i] = c;
    }
    public void removeLayoutComponent(Component c) {
        for (int i = 0; i < _components.length; i++) {
            if (_components[i] == c) {
                _components[i] = null;
                break;
            }
        }
    }
    public void layoutContainer(Container parent) {
        _log.info("layoutContainer("+parent.getName()+")");
        Rectangle bounds = parent.getBounds();
        Insets insets = parent.getInsets();
        // if the parent has zero size in either dimension - ask for minimums
        if (0 == bounds._w || 0 == bounds._h) {
            Dimension d = minimumLayoutSize(parent);
            bounds = new Rectangle(bounds._x, bounds._y, d._w, d._h);
        }
        int top = insets._t;
        int bot = bounds._h - insets._b;
        int left = insets._l;
        int right = bounds._w - insets._r;
        // North component at the top, preferred height, full width
        if (_components[0] != null) {
            Dimension d = _components[0].getPreferredSize();
            _components[0].setBounds(left, top, right-left, d._h);
            top += d._h;
        }
        // South component at the bottom, preferred height, full width
        if (_components[1] != null) {
            Dimension d = _components[1].getPreferredSize();
            _components[1].setBounds(left, bot-d._h, right-left, d._h);
            bot -= d._h;
        }
        // West component at the left, preferred width, remaining height
        if (_components[2] != null) {
            Dimension d = _components[2].getPreferredSize();
            _components[2].setBounds(left, top, d._w, bot-top);
            left += d._w;
        }
        // East component at the right, preferred width, remaining height
        if (_components[3] != null) {
            Dimension d = _components[3].getPreferredSize();
            _components[3].setBounds(right-d._w, top, d._w, bot-top);
            right -= d._w;
        }
        // Center component, remaining space
        if (_components[4] != null) {
            _components[4].setBounds(left, top, right-left, bot-top);
        }
    }
    public Dimension minimumLayoutSize(Container parent) {
        int minW = 0;
        int minH = 0;
        int sumW = 0;
        int sumH = 0;
        for (int i = 0; i < _components.length; i++) {
            if (_components[i] != null) {
                Dimension d = _components[i].getMinimumSize();
                switch (i) {
                case 0:
                    minW = d._w;
                    sumH += d._h;
                    break;
                case 1:
                    minW = d._w > minW ? d._w : minW;
                    sumH += d._h;
                    break;
                case 2:
                    minH = d._h;
                    sumW += d._w;
                    break;
                case 3:
                    minH = d._h > minH ? d._h : minH;
                    sumW += d._w;
                    break;
                case 4:
                    sumW += d._w;
                    sumH += d._h;
                    break;
                }
            }
        }
        if (sumW > minW)
            minW = sumW;
        if (sumH > minH)
            minH = sumH;
        Insets ins = parent.getInsets();
        return new Dimension(minW+ins._l+ins._r, minH+ins._t+ins._b);
    }
}
