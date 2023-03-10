// TODO

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
        int top = 0;
        int bot = bounds._h;
        int left = 0;
        int right = bounds._w;
        // North component at the top, preferred height, full width
        if (_components[0] != null) {
            Dimension d = _components[0].getPreferredSize();
            top += d._h;
            _components[0].setBounds(0, 0, bounds._w, top);
        }
        // South component at the bottom, preferred height, full width
        if (_components[1] != null) {
            Dimension d = _components[1].getPreferredSize();
            bot -= d._h;
            _components[1].setBounds(0, bot, bounds._w, d._h);
        }
        // West component at the left, preferred width, remaining height
        if (_components[2] != null) {
            Dimension d = _components[2].getPreferredSize();
            left += d._w;
            _components[2].setBounds(0, top, left, bot-top);
        }
        // East component at the right, preferred width, remaining height
        if (_components[3] != null) {
            Dimension d = _components[3].getPreferredSize();
            right -= d._w;
            _components[3].setBounds(right, top, d._w, bot-top);
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
        return new Dimension(minW, minH);
    }
}
