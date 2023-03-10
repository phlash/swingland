package com.ashbysoft.swingland;

public interface LayoutManager2 extends LayoutManager {
    void addLayoutComponent(Component c, Object s);
    float getLayoutAlignmentX(Container t);
    float getLayoutAlignmentY(Container t);
    void invalidateLayout(Container t);
    Dimension maximumLayoutSize(Container t);
}
