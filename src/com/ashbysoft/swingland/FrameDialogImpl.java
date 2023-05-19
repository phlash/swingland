package com.ashbysoft.swingland;

// Internal implementation of most of the logic for Frame/Dialog classes, that MUST inherit from Window directly
// to remain compatible with the Swing API.

import com.ashbysoft.swingland.event.AbstractEvent;
import com.ashbysoft.swingland.event.MouseEvent;

public class FrameDialogImpl {
    public static final int BORDER_WIDTH = 4;
    public static final int TITLE_HEIGHT = 20;
    private Window _window;
    private boolean _undecorated;

    // package-private methods for internal helper class
    FrameDialogImpl(Window w, String title) {
        _window = w;
        _window.setVisible(false);
        _window.setTitle(title);
        setUndecorated(false);
    }
    boolean isUndecorated() { return _undecorated; }
    void setUndecorated(boolean undecorated) {
        _window._log.info("FrameDialogImpl:setUndecorated("+undecorated+")");
        _undecorated = undecorated;
        if (undecorated)
            _window.setInsets(null);
        else
            _window.setInsets(new Insets(TITLE_HEIGHT, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        _window.invalidate();
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
    void processEvent(AbstractEvent e) {
        if (!isUndecorated() && _window.isFloating() && e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            Insets i = _window.getInsets();
            if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_MOVE || e.getID() == MouseEvent.MOUSE_DRAGGED) {
                // which cursor do we show?
                int d =
                    m.getY() < i._t ?
                        m.getY() < i._l ?
                            m.getX() < i._t ? SwingConstants.NORTH_WEST : m.getX() >= _window.getWidth() - i._t ? SwingConstants.NORTH_EAST : SwingConstants.NORTH :
                            -1 :
                    m.getY() >= _window.getHeight() - i._b ?
                        m.getX() < i._t ? SwingConstants.SOUTH_WEST : m.getX() >= _window.getWidth() - i._t ? SwingConstants.SOUTH_EAST : SwingConstants.SOUTH :
                        m.getX() < i._l ? SwingConstants.WEST : m.getX() >= _window.getWidth() - i._r ? SwingConstants.EAST : 0;
                if (_resizeDir != d) {
                    _resizeDir = d;
                    _resizeCursor = d > 0 ? Cursor.getPredefinedCursor(_cursorMap[d]) : d < 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
                    _window.drawCursor(_window);
                }
            } else if (e.getID() == MouseEvent.MOUSE_BUTTON) {
                if (_resizeCursor != null) {
                    if (_resizeDir < 0)
                        _window.reposition();
                    else
                        _window.resize(_resizeDir);
                }
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                _resizeDir = 0;
                _resizeCursor = null;
            }
        }
    }
    Cursor getCursor() {
        return _resizeCursor;
    }
    void paint(Graphics g) {
        if (!_window.isVisible())
            return;
        _window._log.info("FrameDialogImpl:paint()");
        // paint our background, decoration (if required) then delegate to Window/Container
        g.setColor(_window.getBackground());
        g.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        if (!isUndecorated()) {
            g.setColor(_window.getForeground());
            g.fillRect(0, 0, _window.getWidth(), TITLE_HEIGHT);
            g.fillRect(0, TITLE_HEIGHT, BORDER_WIDTH, _window.getHeight()-TITLE_HEIGHT-BORDER_WIDTH);
            g.fillRect(_window.getWidth()-BORDER_WIDTH, TITLE_HEIGHT, BORDER_WIDTH, _window.getHeight()-TITLE_HEIGHT-BORDER_WIDTH);
            g.fillRect(0, _window.getHeight()-BORDER_WIDTH, _window.getWidth(), BORDER_WIDTH);
            g.setColor(_window.getBackground());
            g.drawString(_window.getTitle(), BORDER_WIDTH, TITLE_HEIGHT-BORDER_WIDTH);
        }
    }
}
