// XXX:TODO

package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

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
    private ArrayList<KeyListener> _keyListeners;

    protected Component() {
        _name = getClass().getSimpleName()+"@"+hashCode();
        _log = new Logger("["+_name+"]:");
        _visible = true;
        _valid = false;
        _keyListeners = new ArrayList<KeyListener>();
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
    public void setBounds(int x, int y, int w, int h) {
        _log.info("setBounds("+x+","+y+","+w+","+h+")");
        _xrel = x;
        _yrel = y;
        _width = w;
        _height = h;
        invalidate();
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

    // Request a repaint later
    public void repaint() {}
    // Do the painting thing
    public void paint(Graphics g) {}
}
