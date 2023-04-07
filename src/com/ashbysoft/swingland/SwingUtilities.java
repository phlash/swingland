package com.ashbysoft.swingland;

public class SwingUtilities {
    public static void invokeLater(Runnable r) {
        WaylandGlobals.instance().queue(r);
    }
}
