package com.ashbysoft.swingland;

public class JPanel extends JComponent {
    public JPanel() { this(new FlowLayout(), true); }
    public JPanel(boolean db) { this(new FlowLayout(), db); }
    public JPanel(LayoutManager lm) { this(lm, true); }
    public JPanel(LayoutManager lm, boolean db) {
        setLayout(lm);
        setDoubleBuffered(db);
    }
}