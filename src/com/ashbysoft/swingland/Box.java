package com.ashbysoft.swingland;

// Trivial implemenation of the Box conveniance class

public class Box extends JComponent {
    public Box(int axis) {
        super();
        setLayout(new BoxLayout(this, axis));
    }
    public static Component createGlue() { return new GlueComponent(); }
}
