package com.ashbysoft.swingland;

public interface RootPaneContainer {
    Container getContentPane();
    Component getGlassPane();
    JLayeredPane getLayeredPane();
    JRootPane getRootPane();
    void setContentPane(Container cp);
    void setGlassPane(Component gp);
    void setLayeredPane(JLayeredPane lp);
}