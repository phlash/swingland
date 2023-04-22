package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.EventListener;
import java.util.LinkedList;

public class JComponent extends Container {
    private Border _border;
    protected final LinkedList<EventListener> _listeners = new LinkedList<EventListener>();
    public void setDoubleBuffered(boolean db) {}
    public Border getBorder() { return _border; }
    public void setBorder(Border b) {
        _border = b;
        if (_border != null)
            setInsets(_border.getBorderInsets(this));
        else
            setInsets(null);
    }
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        _log.info("JComponent:paint()");
        paintComponent(g);
        paintBorder(g);
        paintChildren(g);
    }
    protected void paintBorder(Graphics g) {
        if (_border != null) {
            _log.info("JComponent:paintBorder()");
            Rectangle bounds = getBounds();
            _border.paintBorder(this, g, 0, 0, bounds._w, bounds._h);
        }
    }
    protected void paintComponent(Graphics g) {
        _log.info("JComponent:paintComponent()");
        if (isBackgroundSet()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    protected void paintChildren(Graphics g) {
        super.paint(g);
    }
    protected void addEventListener(EventListener l) { _listeners.add(l); }
    protected void removeEventListener(EventListener l) { _listeners.remove(l); }
}
