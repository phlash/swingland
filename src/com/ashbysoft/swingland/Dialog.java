package com.ashbysoft.swingland;

public class Dialog extends Window {
    private Window _owner;
    private String _title;
    private boolean _modal;
    private boolean _resizable;
    private boolean _undecorated;

    public Dialog(Window owner) { this(owner, ""); }
    public Dialog(Window owner, String title) { this(owner, title, false); }
    public Dialog(Window owner, String title, boolean modal) {
        _log.info("<init>("+owner.getName()+",'"+title+"',"+modal+")");
        setVisible(false);
        _owner = owner;
        _title = title;
        _modal = modal;
        setUndecorated(false);
    }
    protected Window getOwner() { return _owner; }
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
            setInsets(new Insets(20, 2, 2, 2));  // make room for a simple border + title bar
        repaint();
    }
    public void paint(Graphics g) {
        super.paint(g);
        if (isUndecorated())
            return;
        // NB: need to restore our graphics bounds to draw outside our own insets ;)
        g.setBounds(new Rectangle(0, 0, getWidth(), getHeight()));
        g.setColor(getForeground());
        g.fillRect(0, 0, getWidth(), 20);
        g.fillRect(0, 20, 2, getHeight()-22);
        g.fillRect(getWidth()-2, 20, 2, getHeight()-22);
        g.fillRect(0, getHeight()-2, getWidth(), 2);
        g.setColor(getBackground());
        g.drawString(getTitle(), 2, 18);
    }
}
