package com.ashbysoft.swingland;

public class Dialog extends Window {
    public static final int BORDER_WIDTH = 2;
    public static final int TITLE_HEIGHT = 20;
    private String _title;
    private boolean _modal;
    private boolean _resizable;
    private boolean _undecorated;

    public Dialog(Window owner) { this(owner, ""); }
    public Dialog(Window owner, String title) { this(owner, title, false); }
    public Dialog(Window owner, String title, boolean modal) {
        super(owner);
        _log.info("<init>("+owner.getName()+",'"+title+"',"+modal+")");
        setVisible(false);
        setTitle(title);
        setModal(modal);
        setUndecorated(false);
    }
    public String getTitle() { return _title; }
    public void setTitle(String title) { _title = title; repaint(); }
    public boolean isModal() { return _modal; }
    public void setModal(boolean modal) { _modal = modal; }     // XXX:TODO: work out how to pass down to Wayland grab call.
    public boolean isResizable() { return _resizable; }
    public void setResizable(boolean resizable) { _resizable = resizable; }
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
        // paint our background, border (unless undecorated) then delegate to Window/Container
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
