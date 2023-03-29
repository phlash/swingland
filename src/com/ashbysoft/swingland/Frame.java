// XXX:TODO

package com.ashbysoft.swingland;

public class Frame extends Window {
    public Frame() { this(""); }
    public Frame(String title) {
        setVisible(false);
        setTitle(title);
    }
    public void setTitle(String title) {
        setName(title);
        repaint();
    }
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        // paint our background, then delegate to Window/Container
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }
}
