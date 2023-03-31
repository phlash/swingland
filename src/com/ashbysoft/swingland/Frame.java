package com.ashbysoft.swingland;

public class Frame extends Window {
    private String _title;
    public Frame() { this(""); }
    public Frame(String title) {
        setVisible(false);
        setTitle(title);
    }
    public String getTitle() { return _title; }
    public void setTitle(String title) {
        _title = title;
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
