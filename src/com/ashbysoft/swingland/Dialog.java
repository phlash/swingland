package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;

public class Dialog extends Window {
    private FrameDialogImpl _impl;
    private boolean _modal;
    private boolean _resizable;
    public Dialog(Window owner) { this(owner, ""); }
    public Dialog(Window owner, String title) { this(owner, title, false); }
    public Dialog(Window owner, String title, boolean modal) {
        super(owner);
        _log.info("Dialog:<init>("+owner.getName()+",'"+title+"',"+modal+")");
        _impl = new FrameDialogImpl(this, title);
        setModal(modal);
    }
    public String getTitle() { return super.getTitle(); }
    public void setTitle(String title) { super.setTitle(title); }
    public boolean isModal() { return _modal; }
    public void setModal(boolean modal) {     // TODO: work out how to pass down to Wayland grab call.
        _log.info("Dialog:setModal("+modal+")");
        if (isModal() == modal)
            return;
        _modal = modal;
    }
    public boolean isResizable() { return _resizable; }
    public void setResizable(boolean resizable) {   // :TODO: work out how to inform Wayland we are now re-sizable
        _log.info("Dialog:setResizeable("+resizable+")");
        if (isResizable() == resizable)
            return;
        _resizable = resizable;
    }
    public boolean isUndecorated() { return _impl.isUndecorated(); }
    public void setUndecorated(boolean undecorated) { _impl.setUndecorated(undecorated); }
    protected void processEvent(AbstractEvent e) {
        super.processEvent(e);
        _impl.processEvent(e);
    }
    public Cursor getCursor() {
        Cursor c = _impl.getCursor();
        if (c != null)
            return c;
        return super.getCursor();
    }
    public void paint(Graphics g) {
        _impl.paint(g);
        super.paint(g);
    }
}
