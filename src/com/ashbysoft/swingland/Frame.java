package com.ashbysoft.swingland;

public class Frame extends Window {
    public static final int BORDER_WIDTH = 2;
    public static final int TITLE_HEIGHT = 20;
    private boolean _undecorated;
    public Frame() { this(""); }
    public Frame(String title) {
        setVisible(false);
        setTitle(title);
        setUndecorated(false);
    }
    public Frame(Window owner, String title) {
        super(owner);
        setVisible(false);
        setTitle(title);
        setUndecorated(false);
    }
    public String getTitle() { return super.getTitle(); }
    public void setTitle(String title) {
        super.setTitle(title);
    }
    public boolean isUndecorated() { return _undecorated; }
    public void setUndecorated(boolean undecorated) {
        _undecorated = undecorated;
        if (undecorated)
            setInsets(null);
        else
            setInsets(new Insets(TITLE_HEIGHT, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        invalidate();
    }
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        // paint our background, decoration (if required) then delegate to Window/Container
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (!isUndecorated()) {
            g.setColor(getForeground());
            g.fillRect(0, 0, getWidth(), TITLE_HEIGHT);
            g.fillRect(0, TITLE_HEIGHT, BORDER_WIDTH, getHeight()-TITLE_HEIGHT-BORDER_WIDTH);
            g.fillRect(getWidth()-BORDER_WIDTH, TITLE_HEIGHT, BORDER_WIDTH, getHeight()-TITLE_HEIGHT-BORDER_WIDTH);
            g.fillRect(0, getHeight()-BORDER_WIDTH, getWidth(), BORDER_WIDTH);
            g.setColor(getBackground());
            g.drawString(getTitle(), BORDER_WIDTH, TITLE_HEIGHT-BORDER_WIDTH);
        }
        super.paint(g);
    }
}
