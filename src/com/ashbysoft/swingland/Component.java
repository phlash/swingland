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
    private boolean _enabled;
    private Color _background;
    private Color _foreground;
    private Font _font;
    private Cursor _cursor;
    private ArrayList<KeyListener> _keyListeners;
    private ArrayList<MouseInputListener> _mouseListeners;
    private ArrayList<MouseWheelListener> _wheelListeners;
    private ArrayList<Integer> _mouseButtons;
    private boolean _seenMouse;
    private static Component s_lastEntered;

    protected Component() {
        _name = getClass().getSimpleName()+"@"+hashCode();
        _log = new Logger("["+_name+"]:");
        _log.info("Component:<init>()");
        _visible = true;
        _enabled = true;
        _valid = false;
        _keyListeners = new ArrayList<>();
        _mouseListeners = new ArrayList<>();
        _wheelListeners = new ArrayList<>();
        _mouseButtons = new ArrayList<>();
        _seenMouse = false;
    }
    public Container getParent() { return _parent; }
    // package-private method for Container.addImpl use
    void setParent(Container p) {
        _log.info("Component:setParent("+(p!=null?p.getName():"null")+")");
        _parent = p;
        invalidate();
    }
    // are we enabled (will process input events / generate action events, often 'greys out' when disabled)
    public boolean isEnabled() { return _enabled; }
    public void setEnabled(boolean e) {
        _log.info("Component:setEnabled("+e+")");
        _enabled = e;
    }
    // are we visible (provided all parent objects are visible)?
    public boolean isVisible() { return _visible; }
    public void setVisible(boolean v) {
        _log.info("Component:setVisible("+v+")");
        _visible = v;
    }
    // are we showing (check parent objects)
    public boolean isShowing() {
        Container p = getParent();
        if (!isVisible() || null == p)
            return false;
        return p.isShowing();
    }
    // what's this instance called (mostly affects logging)?
    public String getName() { return _name; }
    public void setName(String n) {
        _log.info("Component:setName("+n+")");
        _log.setPfx(n);
        _name = n;
    }
    // all renderable components have a graphics config, basic colours, a font, a cursor.. or their parent might!
    public GraphicsConfiguration getGraphicsConfiguration() {
        Container p = getParent();
        if (p != null)
            return p.getGraphicsConfiguration();
        return null;
    }
    public boolean isBackgroundSet() { return _background != null; }
    public Color getBackground() {
        if (_background != null)
            return _background;
        Container p = getParent();
        if (p != null)
            return p.getBackground();
        return null;
    }
    public boolean isForegroundSet() { return _foreground != null; }
    public Color getForeground() {
        if (_foreground != null)
            return _foreground;
        Container p = getParent();
        if (p != null)
            return p.getForeground();
        return null;
    }
    public boolean isFontSet() { return _font != null; }
    public Font getFont() {
        if (_font != null)
            return _font;
        Container p = getParent();
        if (p != null)
            return p.getFont();
        return null;
    }
    public boolean isCursorSet() { return _cursor != null; }
    public Cursor getCursor() {
        if (_cursor != null)
            return _cursor;
        Container p = getParent();
        if (p != null)
            return p.getCursor();
        return null;
    }
    public void setBackground(Color c) {
        _log.info("Component:setBackground("+c+")");
        _background = c;
    }
    public void setForeground(Color c) {
        _log.info("Component:setForeground("+c+")");
        _foreground = c;
    }
    public void setFont(Font f) {
        _log.info("Component:setFont("+f+")");
        _font = f;
    }
    public void setCursor(Cursor c) {
        _log.info("Component:setCursor("+c+")");
        _cursor = c;
    }

    public boolean isPreferredSizeSet() { return _prefSize != null; }
    public boolean isMinimumSizeSet() { return _minSize != null; }
    public boolean isMaximumSizeSet() { return _maxSize != null; }
    public Dimension getPreferredSize() { return _prefSize != null ? _prefSize : getMinimumSize(); }
    public Dimension getMinimumSize() { return _minSize != null ? _minSize : new Dimension(_width, _height); }
    public Dimension getMaximumSize() { return _maxSize != null ? _maxSize : new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    public void setPreferredSize(Dimension d) {
        _log.info("Component:setPreferredSize("+d+")");
        _prefSize = d;
        invalidate();
    }
    public void setMinimumSize(Dimension d) {
        _log.info("Component:setMinimumSize("+d+")");
        _minSize = d;
        invalidate();
    }
    public void setMaximumSize(Dimension d) {
        _log.info("Component:setMaximumSize("+d+")");
        _maxSize = d;
        invalidate();
    }
    public int getX() { return _xrel; }
    public int getY() { return _yrel; }
    public int getWidth() { return _width; }
    public int getHeight() { return _height; }
    public Dimension getSize() { return new Dimension(_width, _height); }
    public Point getLocation() { return new Point(_xrel, _yrel); }
    public Rectangle getBounds() { return new Rectangle(_xrel, _yrel, _width, _height); }
    public void setSize(int w, int h) {
        _log.info("Component:setSize("+w+","+h+")");
        if (getWidth() != w || getHeight() != h) {
            _width = w; _height = h;
            invalidate();
        }
    }
    public void setSize(Dimension d) { setSize(d._w, d._h); }
    public void setLocation(int x, int y) {
        _log.info("Component:setLocation("+x+","+y+")");
        if (getX() != x || getY() != y) {
            _xrel = x;
            _yrel = y;
            invalidate();
        }
    }
    public void setLocation(Point p) { setLocation(p._x, p._y); }
    public void setBounds(int x, int y, int w, int h) {
        _log.info("Component:setBounds("+x+","+y+","+w+","+h+")");
        if (getX() != x || getY() != y || getWidth() != w || getHeight() != h) {
            _xrel = x;
            _yrel = y;
            _width = w;
            _height = h;
            invalidate();
        }
    }
    public boolean hasFocus() {
        Container p = getParent();
        if (p != null)
            return p.hasFocus(this);
        return false;
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
        _log.info("Component:invalidate()");
        Component p = getParent();
        if (p!=null)
            p.invalidate();
        // mark ourselves as invalid
        _valid = false;
    }
    public void validate() {
        // if we were invalid, simply mark ourselves as valid
        if (!_valid) {
            _log.info("Component:validate()");
            _valid = true;
        }
    }
    public void addKeyListener(KeyListener l) {
        _log.info("Component:addKeyListener("+l.getClass().getSimpleName()+")");
        if (!_keyListeners.contains(l))
            _keyListeners.add(l);
    }
    public void removeKeyListener(KeyListener l) {
        _log.info("Component:removeKeyListener("+l.getClass().getSimpleName()+")");
        if (_keyListeners.contains(l))
            _keyListeners.remove(l);
    }
    public void addMouseInputListener(MouseInputListener l) {
        _log.info("Component:addMouseInputListener("+l.getClass().getSimpleName()+")");
        if (!_mouseListeners.contains(l))
            _mouseListeners.add(l);
    }
    public void removeMouseInputListener(MouseInputListener l) {
        _log.info("Component:removeMouseInputListener("+l.getClass().getSimpleName()+")");
        if (_mouseListeners.contains(l))
            _mouseListeners.remove(l);
    }
    public void addMouseWheelListener(MouseWheelListener l) {
        _log.info("Component:addMouseWheelListener("+l.getClass().getSimpleName()+")");
        if (!_wheelListeners.contains(l))
            _wheelListeners.add(l);
    }
    public void removeMouseWheelListener(MouseWheelListener l) {
        _log.info("Component:removeMouseWheelListener("+l.getClass().getSimpleName()+")");
        if (_wheelListeners.contains(l))
            _wheelListeners.remove(l);
    }
    // separate public method so we can recurse internally via a private method without
    // invoking overridden public methods
    public void dispatchEvent(AbstractEvent e) {
        dispatchEventImpl(e);
    }
    private void dispatchEventImpl(AbstractEvent e) {
        _log.info("Component:dispatchEvent(sm="+_seenMouse+"):"+e.toString());
        // dispatch to any listeners, so they can consume the event, before any local processing
        if (e instanceof KeyEvent) {
            KeyEvent k = (KeyEvent)e;
            // ensure we are the event source when transitioning from dispatch flow to handlers
            k = new KeyEvent(this, k.getID(), k.getModifiersEx(), k.getKeyCode(), k.getKeyChar());
            // copy down event internal state
            k.copyState(e);
            for (KeyListener l : _keyListeners) {
                if (KeyEvent.KEY_RELEASED == k.getID())
                    l.keyReleased(k);
                else if (KeyEvent.KEY_PRESSED == k.getID())
                    l.keyPressed(k);
                else
                    l.keyTyped(k);
            }
            // local processing (if any) after listeners
            if (!k.isConsumed())
                processEvent(k);
            // copy out internal state to caller
            e.copyState(k);
        } else if (e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            // ensure we are the event source when transitioning from dispatch flow to handlers
            m = m instanceof MouseWheelEvent ?
                new MouseWheelEvent(this, m.getID(), m.getModifiersEx(), m.getX(), m.getY(), m.getButton(), m.getState(), ((MouseWheelEvent)m).getWheelRotation()) :
                new MouseEvent(this, m.getID(), m.getModifiersEx(), m.getX(), m.getY(), m.getButton(), m.getState());
            // copy down event internal state
            m.copyState(e);
            // first mouse event? might need to synthesize MOUSE_ENTERED..
            if (!_seenMouse && m.getCanSynthesize() && MouseEvent.MOUSE_ENTERED != m.getID() && MouseEvent.MOUSE_EXITED != m.getID()) {
                _seenMouse = true;
                // notify the previously entered Component that the mouse has left the building..
                if (s_lastEntered != null) {
                    _log.info("- synthesize EXITED -> "+s_lastEntered.getName());
                    // We supply invalid co-ordinates here, as the mouse has left the building!
                    s_lastEntered.dispatchEventImpl(new MouseEvent(m.getSource(), MouseEvent.MOUSE_EXITED, m.getModifiersEx(), -1, -1, 0, 0));
                }
                // mark ourselves as the last notified Component
                s_lastEntered = this;
                // recursively notify ourselves of mouse entry
                _log.info("- synthesize ENTERED (this)");
                dispatchEventImpl(new MouseEvent(m.getSource(), MouseEvent.MOUSE_ENTERED, m.getModifiersEx(), m.getX(), m.getY(), m.getButton(), m.getState()));
                // update the cursor if required
                drawCursor(this);
            }
            // process the listeners
            if (m instanceof MouseWheelEvent) {
                for (var l : _wheelListeners)
                    l.mouseWheelMoved((MouseWheelEvent)m);
            } else {
                for (var l : _mouseListeners) {
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
                    } else if (MouseEvent.MOUSE_DRAGGED == m.getID()) {
                        l.mouseDragged(m);
                    }
                }
            }
            // local processing (if any) after listeners
            if (!m.isConsumed())
                processEvent(m);
            // process button press/release into clicks and recursively dispatch
            if (MouseEvent.MOUSE_BUTTON == m.getID()) {
                if (MouseEvent.BUTTON_PRESSED == m.getState()) {
                    _mouseButtons.add(m.getButton());
                } else if (_mouseButtons.contains(m.getButton())) {
                    _mouseButtons.remove((Integer)m.getButton());
                    if (m.getCanSynthesize()) {
                        _log.info("- synthesize CLICKED (this)");
                        dispatchEventImpl(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, m.getModifiersEx(), m.getX(), m.getY(), m.getButton(), 0));
                    }
                }
            } else if (MouseEvent.MOUSE_EXITED == m.getID()) {
                _mouseButtons.clear();
                _seenMouse = false;
                s_lastEntered = null;
            }
            // mark the event object to prevent further synthesis by parents
            m.setCanSynthesize(false);
            // copy out internal state to caller
            e.copyState(m);
        }
    }
    // process an event locally (eg: button push)
    protected void processEvent(AbstractEvent e) {}

    // Request a repaint later
    public void repaint() {
        _log.info("Component:repaint()");
        // delegate to container by default
        Container p = getParent();
        if (p != null)
            p.repaint();
    }
    // Do the painting thing
    public void paint(Graphics g) {}

    // Package-private cursor update
    void drawCursor(Component src) {
        _log.info("Component:drawCursor("+src.getName()+")");
        Container p = getParent();
        if (p != null)
            p.drawCursor(src);
    }
}
