package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;

public class NullLayout implements LayoutManager2 {
    protected Logger _log = new Logger("["+getClass().getSimpleName()+"@"+hashCode()+"]:");
    // LayoutManager
    public void addLayoutComponent(String name, Component c) {}
    public void layoutContainer(Container parent) {
        _log.info("NullLayout:layoutContainer("+parent.getName()+")");
    }
    public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,0); }
    public Dimension preferredLayoutSize(Container parent) { return minimumLayoutSize(parent); }
    public void removeLayoutComponent(Component c) {}
    // LayoutManager2
    public void addLayoutComponent(Component c, Object s) {}
    public float getLayoutAlignmentX(Container t) { return 0.0f; }
    public float getLayoutAlignmentY(Container t) { return 0.0f; }
    public void invalidateLayout(Container t) {}
    public Dimension maximumLayoutSize(Container t) { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
}
