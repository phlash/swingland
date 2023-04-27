package com.ashbysoft.swingland;

public class JSeparator extends JComponent {
    private int _orientation;
    public JSeparator() { this(SwingConstants.HORIZONTAL); }
    public JSeparator(int orientation) { _orientation = orientation; }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getForeground());
        if (SwingConstants.HORIZONTAL == _orientation)
            g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
        else
            g.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
    }
}
