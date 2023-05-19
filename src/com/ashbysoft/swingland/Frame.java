package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;

public class Frame extends Window {
    private FrameDialogImpl _impl;
    public Frame() { this(""); }
    public Frame(String title) { this(null, title); }
    public Frame(Window owner, String title) {
        super(owner);
        _log.info("Frame:<init>("+(owner != null ? owner.getName() : "null")+","+title+")");
        _impl = new FrameDialogImpl(this, title);
    }
    public String getTitle() { return super.getTitle(); }
    public void setTitle(String title) { super.setTitle(title); }
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
