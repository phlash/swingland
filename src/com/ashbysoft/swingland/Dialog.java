package com.ashbysoft.swingland;

public class Dialog extends Window {
    public static final int BORDER_WIDTH = 2;
    public static final int TITLE_HEIGHT = 20;
    private boolean _modal;
    private boolean _resizable;
    private boolean _undecorated;

    public Dialog(Window owner) { this(owner, ""); }
    public Dialog(Window owner, String title) { this(owner, title, false); }
    public Dialog(Window owner, String title, boolean modal) {
        super(owner);
        _log.info("Dialog:<init>("+owner.getName()+",'"+title+"',"+modal+")");
        setVisible(false);
        setTitle(title);
        setModal(modal);
        setUndecorated(false);
    }
    public String getTitle() { return super.getTitle(); }
    public void setTitle(String title) { super.setTitle(title); }
    public boolean isModal() { return _modal; }
    public void setModal(boolean modal) {     // XXX:TODO: work out how to pass down to Wayland grab call.
        _log.info("Dialog:setModal("+modal+")");
        if (isModal() == modal)
            return;
        _modal = modal;
    }
    public boolean isResizable() { return _resizable; }
    public void setResizable(boolean resizable) {   // XXX::TODO: work out how to inform Wayland we are now re-sizable
        _log.info("Dialog:setResizeable("+resizable+")");
        if (isResizable() == resizable)
            return;
        _resizable = resizable;
    }
    public boolean isUndecorated() { return _undecorated; }
    public void setUndecorated(boolean undecorated) {
        _log.info("Dialog:setUndecorated("+undecorated+")");
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
        _log.info("Dialog:paint()");
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
