package com.ashbysoft.swingland;

public interface LayoutManager {
    void addLayoutComponent(String name, Component comp);
    void layoutContainer(Container parent);
    Dimension minimumLayoutSize(Container parent);
    Dimension preferredLayoutSize(Container parent);
    void removeLayoutComponent(Component comp);
}
