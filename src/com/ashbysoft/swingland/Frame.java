package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;
import com.ashbysoft.swingland.event.MouseEvent;

public class Frame extends Window {
    public static final int BORDER_WIDTH = 4;
    public static final int TITLE_HEIGHT = 20;
    private boolean _undecorated;
    public Frame() { this(""); }
    public Frame(String title) { this(null, title); }
    public Frame(Window owner, String title) {
        super(owner);
        _log.info("Frame:<init>("+(owner != null ? owner.getName() : "null")+","+title+")");
        setVisible(false);
        setTitle(title);
        setUndecorated(false);
    }
    public String getTitle() { return super.getTitle(); }
    public void setTitle(String title) { super.setTitle(title); }
    public boolean isUndecorated() { return _undecorated; }
    public void setUndecorated(boolean undecorated) {
        _log.info("Frame:setUndecorated("+undecorated+")");
        _undecorated = undecorated;
        if (undecorated)
            setInsets(null);
        else
            setInsets(new Insets(TITLE_HEIGHT, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        invalidate();
    }
    // intercept local event processing - detect mouse over decorated frame of re-sizeable window and update cursor
    private int _resizeDir;
    private Cursor _resizeCursor;
    private static int[] _cursorMap = {
        0,
        Cursor.N_RESIZE_CURSOR,
        Cursor.NE_RESIZE_CURSOR,
        Cursor.E_RESIZE_CURSOR,
        Cursor.SE_RESIZE_CURSOR,
        Cursor.S_RESIZE_CURSOR,
        Cursor.SW_RESIZE_CURSOR,
        Cursor.W_RESIZE_CURSOR,
        Cursor.NW_RESIZE_CURSOR
    };
    protected void processEvent(AbstractEvent e) {
        super.processEvent(e);
        if (!isUndecorated() && isFloating() && e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            Insets i = getInsets();
            if (e.getID() == MouseEvent.MOUSE_MOVE || e.getID() == MouseEvent.MOUSE_DRAGGED) {
                // which cursor do we show?
                int d = m.getY() < i._t ?
                    m.getY() < i._l ?
                        m.getX() < i._t ? SwingConstants.NORTH_WEST : m.getX() >= getWidth() - i._t ? SwingConstants.NORTH_EAST : SwingConstants.NORTH :
                        -1 :
                m.getY() >= getHeight() - i._b ?
                    m.getX() < i._t ? SwingConstants.SOUTH_WEST : m.getX() >= getWidth() - i._t ? SwingConstants.SOUTH_EAST : SwingConstants.SOUTH :
                    m.getX() < i._l ? SwingConstants.WEST : m.getX() >= getWidth() - i._r ? SwingConstants.EAST : 0;
                if (_resizeDir != d) {
                    _resizeDir = d;
                    _resizeCursor = d > 0 ? Cursor.getPredefinedCursor(_cursorMap[d]) : d < 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
                    drawCursor(this);
                }
            } else if (e.getID() == MouseEvent.MOUSE_BUTTON) {
                if (_resizeCursor != null) {
                    if (_resizeDir < 0)
                        reposition();
                    else
                        resize(_resizeDir);
                }
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                _resizeDir = 0;
                _resizeCursor = null;
            }
        }
    }
    public Cursor getCursor() {
        if (_resizeCursor != null)
            return _resizeCursor;
        return super.getCursor();
    }
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        _log.info("Frame:paint()");
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
