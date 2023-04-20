package com.ashbysoft.swingland;

// An elastic space that adjusts it's minimum/preferred size to occupy remaining space in container

public class GlueComponent extends Component {
    public Dimension getPreferredSize() { return getMinimumSize(); }
    public Dimension getMinimumSize() {
        Container p = getParent();
        if (null == p)
            return new Dimension(0, 0);
        int sumX = 0;
        int sumY = 0;
        for (int i = 0; i < p.getComponentCount(); i += 1) {
            Component c = p.getComponent(i);
            if (this == c)
                continue;
            Dimension d = c.getPreferredSize();
            sumX += d._w;
            sumY += d._h;
        }
        Dimension pd = p.getSize();
        return new Dimension(pd._w - sumX, pd._h - sumY);
    }
}
