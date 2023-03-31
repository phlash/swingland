package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;
import com.ashbysoft.logger.Logger;
import java.util.ArrayList;

public abstract class Component {
    private Container _parent;
    private String _name;
    protected Logger _log;
    private Dimension _prefSize;
    private Dimension _minSize;
    private Dimension _maxSize;
    private int _xrel;
    private int _yrel;
    private int _width;
    private int _height;
    private boolean _valid;
    private boolean _visible;
    private Color _background;
    private Color _foreground;
    private ArrayList<KeyListener> _keyListeners;
    private ArrayList<MouseInputListener> _mouseListeners;
    private ArrayList<Integer> _mouseButtons;

    protected Component() {
        _name = getClass().getSimpleName()+"@"+hashCode();
        _log = new Logger("["+_name+"]:");
        _visible = true;
        _valid = false;
        _background = Color.GRAY;
        _foreground = Color.BLACK;
        _keyListeners = new ArrayList<KeyListener>();
        _mouseListeners = new ArrayList<MouseInputListener>();
        _mouseButtons = new ArrayList<Integer>();
    }
    public Container getParent() { return _parent; }
    // package-private method for Container.addImpl use
    void setParent(Container p) {
        _log.info("setParent("+(p!=null?p.getName():"null")+")");
        _parent = p;
        invalidate();
    }
    // just-enough-to-compile methods..
    public boolean isVisible() { return _visible; }
    public void setVisible(boolean v) {
        _log.info("setVisible("+v+")");
        _visible = v;
    }

    public String getName() { return _name; }
    public void setName(String n) {
        _log.info("setName("+n+")");
        _name = n;
    }
    public Color getBackground() { return _background; }
    public Color getForeground() { return _foreground; }
    public void setBackground(Color c) { _background = c; }
    public void setForeground(Color c) { _foreground = c; }

    public Dimension getPreferredSize() { return _prefSize != null ? _prefSize : _parent != null ? _parent.getSize() : getMinimumSize(); }
    public Dimension getMinimumSize() { return _minSize != null ? _minSize : new Dimension(_width, _height); }
    public Dimension getMaximumSize() { return _maxSize != null ? _maxSize : new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    public void setPreferredSize(Dimension d) {
        _log.info("setPreferredSize("+d+")");
        _prefSize = d;
        invalidate();
    }
    public void setMinimumSize(Dimension d) {
        _log.info("setMinimumSize("+d+")");
        _minSize = d;
        invalidate();
    }
    public void setMaximumSize(Dimension d) {
        _log.info("setMaximumSize("+d+")");
        _maxSize = d;
        invalidate();
    }
    public int getX() { return _xrel; }
    public int getY() { return _yrel; }
    public int getWidth() { return _width; }
    public int getHeight() { return _height; }
    public Dimension getSize() { return new Dimension(_width,_height); }
    public Point getLocation() { return new Point(_xrel, _yrel); }
    public Rectangle getBounds() { return new Rectangle(_xrel, _yrel, _width, _height); }
    public void setSize(int w, int h) {
        _log.info("setSize("+w+","+h+")");
        _width = w; _height = h;
        invalidate();
    }
    public void setSize(Dimension d) { setSize((int)d.getWidth(), (int)d.getHeight()); }
    public void setLocation(int x, int y) {
        _log.info("setLocation("+x+","+y+")");
        _xrel = x;
        _yrel = y;
        invalidate();
    }
    public void setLocation(Point p) { setLocation(p._x, p._y); }
    public void setBounds(int x, int y, int w, int h) {
        _log.info("setBounds("+x+","+y+","+w+","+h+")");
        _xrel = x;
        _yrel = y;
        _width = w;
        _height = h;
        invalidate();
    }

    protected Graphics getGraphics() {
        Container p = getParent();
        if (p != null)
            return p.getGraphics();
        return null;
    }
    public boolean isValid() { return _valid; }
    public void invalidate() {
        // ignore if already invalid
        if (!_valid)
            return;
        // if we are part of a component tree, invalidate upwards
        _log.info("invalidate()");
        Component p = getParent();
        if (p!=null)
            p.invalidate();
        // mark ourselves as invalid
        _valid = false;
    }
    public void validate() {
        // if we were invalid, simply mark ourselves as valid
        if (!_valid) {
            _log.info("validate()");
            _valid = true;
        }
    }
    public void addKeyListener(KeyListener l) {
        _log.info("addKeyListener("+l.getClass().getSimpleName()+")");
        if (!_keyListeners.contains(l))
            _keyListeners.add(l);
    }
    public void removeKeyListener(KeyListener l) {
        _log.info("removeKeyListener("+l.getClass().getSimpleName()+")");
        if (_keyListeners.contains(l))
            _keyListeners.remove(l);
    }
    public void addMouseInputListener(MouseInputListener l) {
        _log.info("addMouseInputListener("+l.getClass().getSimpleName()+")");
        if (!_mouseListeners.contains(l))
            _mouseListeners.add(l);
    }
    public void removeMouseInputListener(MouseInputListener l) {
        _log.info("removeMouseInputListener("+l.getClass().getSimpleName()+")");
        if (_mouseListeners.contains(l))
            _mouseListeners.remove(l);
    }
    // dispath to any listeners, so they can consume the event, before any local processing
    public void dispatchEvent(AbstractEvent e) {
        dispatchEventImpl(e);
    }
    private void dispatchEventImpl(AbstractEvent e) {
        if (e instanceof KeyEvent) {
            KeyEvent k = (KeyEvent)e;
            k = new KeyEvent(this, k.getID(), k.getKeyCode(), k.getKeyChar());
            for (KeyListener l : _keyListeners) {
                if (KeyEvent.KEY_RELEASED == k.getID())
                    l.keyReleased(k);
                else if (KeyEvent.KEY_PRESSED == k.getID())
                    l.keyPressed(k);
                else
                    l.keyTyped(k);
            }
        } else if (e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            m = new MouseEvent(this, m.getID(), m.getX(), m.getY(), m.getButton(), m.getState());
            for (MouseInputListener l : _mouseListeners) {
                if (MouseEvent.MOUSE_MOVE == m.getID()) {
                    l.mouseMoved(m);
                } else if (MouseEvent.MOUSE_BUTTON == m.getID()) {
                    if (MouseEvent.BUTTON_RELEASED == m.getState())
                        l.mouseReleased(m);
                    else
                        l.mousePressed(m);
                } else if (MouseEvent.MOUSE_ENTERED == m.getID()) {
                    l.mouseEntered(m);
                } else if (MouseEvent.MOUSE_EXITED == m.getID()) {
                    l.mouseExited(m);
                } else if (MouseEvent.MOUSE_CLICKED == m.getID()) {
                    l.mouseClicked(m);
                }
            }
            // process button press/release into clicks and recursively dispatch
            if (MouseEvent.MOUSE_BUTTON == m.getID()) {
                if (MouseEvent.BUTTON_PRESSED == m.getState()) {
                    _mouseButtons.add(m.getButton());
                } else if (_mouseButtons.contains(m.getButton())) {
                    _mouseButtons.remove((Integer)m.getButton());
                    dispatchEventImpl(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, m.getX(), m.getY(), m.getButton(), 0));
                }
            } else if (MouseEvent.MOUSE_EXITED == m.getID()) {
                _mouseButtons.clear();
            }
        }
        // unless consumed, process locally
        if (!e.isConsumed())
            processEvent(e);
    }
    // process an event locally (eg: button push)
    public void processEvent(AbstractEvent e) {}

    // Request a repaint later
    public void repaint() {
        // delegate to container by default
        Component p = getParent();
        if (p != null)
            p.repaint();
    }
    // Do the painting thing
    public void paint(Graphics g) {}
}
