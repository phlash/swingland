package com.ashbysoft.swingland;

// All renderable objects (Frame, Dialog, etc.) are based on Window. So this
// is where we choose to connect across to Wayland, holding the drawing surface
// and performing rendering operations.

import com.ashbysoft.wayland.Surface;
import com.ashbysoft.wayland.XdgSurface;
import com.ashbysoft.wayland.XdgToplevel;
import com.ashbysoft.wayland.XdgPopup;
import com.ashbysoft.wayland.ShmPool;
import com.ashbysoft.wayland.Buffer;
import com.ashbysoft.wayland.Positioner;
import com.ashbysoft.swingland.event.WindowEvent;
import com.ashbysoft.swingland.event.WindowListener;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class Window extends Container implements
    Surface.Listener,
    XdgSurface.Listener,
    XdgToplevel.Listener,
    XdgPopup.Listener {
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT= 480;
    public static final Color DEFAULT_BACKGROUND = Color.LIGHT_GRAY;
    public static final Color DEFAULT_FOREGROUND = Color.BLACK;
    public static final Color DEFAULT_DISABLED = Color.GRAY;
    public static final String DEFAULT_FONT = Font.MONOSPACED;

    // Wayland objects instantiated per-window
    private WaylandGlobals _g;
    private GraphicsConfiguration _config;
    private Surface _surface;
    private XdgSurface _xdgSurface;
    private XdgToplevel _xdgToplevel;
    private XdgPopup _xdgPopup;
    private int _poolsize;
    private ShmPool _shmpool;
    private Buffer _buffer;
    private int _lastCursor;
    // original size (if any), used during configure callback
    private int _origWidth;
    private int _origHeight;
    // window ownership hierarchy, affects lifecycle / visibility methods
    private Window _owner;
    private LinkedList<Window> _owned;
    // window event listeners
    private LinkedList<WindowListener> _listeners;
    private boolean _hasOpened;
    // popup window (short-lived, eg: menus, tooltips)
    private boolean _isPopup;
    // display states
    private boolean _isActive;
    private boolean _isFloating;
    private boolean _isResizing;
    private boolean _isMaximized;
    private boolean _isFullscreen;
    // title text - here as it's passed to Wayland
    private String _title;

    protected Window() { this(null); }
    protected Window(Window owner) { this(owner, false); }
    protected Window(Window owner, boolean isPopup) { this(owner, isPopup, null); }
    protected Window(Window owner, boolean isPopup, GraphicsConfiguration config) {
        _log.info("Window:<init>("+(owner != null ? owner.getName() : "null")+","+isPopup+","+(config != null ? config : "null")+")");
        if (owner != null) {
            _owner = owner;
            _owner.addOwned(this);    
        } else if (isPopup) {
            throw new IllegalArgumentException("popup windows require an owner");
        }
        _isPopup = isPopup;
        _config = (config != null) ? config : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        _owned = new LinkedList<>();
        _listeners = new LinkedList<>();
        // grab a reference to Wayland
        _g = WaylandGlobals.instance();
        // default rendering properties
        setBackground(DEFAULT_BACKGROUND);
        setForeground(DEFAULT_FOREGROUND);
        setFont(Font.getFont(DEFAULT_FONT));
        setCursor(Cursor.getDefaultCursor());
        _lastCursor = -1;
        _hasOpened = false;
    }

    // package-private UI thread callback to render things!
    void render() {
        _log.info("-->render()");
        // check Wayland is done with previous buffer (if any)
        if (_buffer != null && _buffer.isBusy()) {
            _log.error("render overrun");
            // pop ourselves back on the queue for later
            repaint();
            return;
        }
        // buffer size changed?
        int psize = getWidth() * getHeight() * 4;
        if (psize != _poolsize) {
            _log.info("-size:"+_poolsize+"->"+psize);
            // drop existing pool if any
            if (_buffer != null)
                _buffer.destroy();
            if (_shmpool != null)
                _shmpool.destroy();
            _poolsize = psize;
            _shmpool = _g.shm().createPool(_poolsize);
            _buffer = _shmpool.createBuffer(0, getWidth(), getHeight(), getWidth() * 4, 0);
        } else if (_buffer != null) {
            // always start with an empty buffer, avoids issues with alpha compositing over earlier frames!
            ByteBuffer b = _buffer.get();
            b.clear();
            for (int i = 0; i < b.limit(); i += 1)
                b.put(i, (byte)0);
        }
        if (_buffer != null) {
            validate();
            paint(getGraphics());
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, getWidth(), getHeight());
        }
        _surface.commit();
        _log.info("<--render()");
    }
    // cursor rendering
    protected void drawCursor(Component src) {
        _log.info("Window:drawCursor("+src.getName()+")");
        Cursor c = src.getCursor();
        _log.info("-c="+c.toString());
        // no cursor, or nothing changed
        if (null == c || _lastCursor == c.getType())
            return;
        Dimension d = c.getSize();
        ShmPool pool;
        Buffer buffer;
        // got old things?
        Cursor.Resources res = c.getResources();
        if (res != null && res instanceof CursorBuffer) {
            pool = ((CursorBuffer)res)._pool;
            buffer = ((CursorBuffer)res)._buffer;
        } else {
        // make new things..
            pool = _g.shm().createPool(d._w * d._h * 4);
            buffer = pool.createBuffer(0, d._w, d._h, d._w * 4, 0);
            c.setResources(new CursorBuffer(pool, buffer));
        }
        // draw the cursor
        c.drawCursor(new Graphics(buffer.get(), d._w, d._h, null, null));
        _lastCursor = c.getType();
        // push to Wayland
        _g.updateCursor(buffer, d, c.getHotspot());
    }
    private class CursorBuffer implements Cursor.Resources {
        private ShmPool _pool;
        private Buffer _buffer;
        public CursorBuffer(ShmPool pool, Buffer buffer) { _pool = pool; _buffer = buffer; }
        public void destroy() {
            if (_buffer != null) {
                _buffer.destroy();
                _buffer = null;
            }
            if (_pool != null) {
                _pool.destroy();
                _pool = null;
            }
        }
    }
    private void toWayland() {
        _log.info("toWayland()");
        // save original size..
        _origWidth = getWidth();
        _origHeight= getHeight();
        _log.info("-orig:"+_origWidth+","+_origHeight);
        // hook us up to the real world!
        Positioner positioner = null;
        if (_isPopup) {
            // NB: we do this /before/ any surface stuff, or Sway breaks :(
            positioner = new Positioner(_g.display());
            _g.xdgWmBase().createPositioner(positioner);
            // the size of this popup window
            positioner.setSize(getWidth(), getHeight());
            // assumes *our* position has been set relative to owning surface
            positioner.setAnchorRect(getX(), getY(), getWidth(), getHeight());
            // default anchor corner & gravity (both 'centre') will put us in the rect above.
        }
        _surface = new Surface(_g.display());
        _surface.addListener(this);
        _g.compositor().createSurface(_surface);
        _g.register(_surface.getID(), this);
        _xdgSurface = new XdgSurface(_g.display());
        _xdgSurface.addListener(this);
        _g.xdgWmBase().getXdgSurface(_xdgSurface, _surface);
        // if requested, create a popup rather than a top level window
        if (_isPopup) {
            _xdgPopup = new XdgPopup(_g.display());
            _xdgPopup.addListener(this);
            _xdgSurface.getPopup(_xdgPopup, _owner._xdgSurface, positioner);
            _g.pushPopup(this);
            positioner.destroy();
        } else {
            _xdgToplevel = new XdgToplevel(_g.display());
            _xdgToplevel.addListener(this);
            _xdgSurface.getTopLevel(_xdgToplevel);
            if (_owner != null)
                _xdgToplevel.setParent(_owner._xdgToplevel);
            if (_title != null)
                _xdgToplevel.setTitle(_title);
            if (getWidth() > 0 && getHeight() > 0) {
                // fix size - this also makes it float in Sway
                _xdgToplevel.setMaxSize(getWidth(), getHeight());
                _xdgToplevel.setMinSize(getWidth(), getHeight());
            }
        }
        _surface.commit();
    }
    private void fromWayland() {
        _log.info("fromWayland()");
        // run away!
        if (_buffer != null) {
            _buffer.destroy();
            _buffer = null;
        }
        if (_shmpool != null) {
            _shmpool.destroy();
            _shmpool = null;
        }
        _poolsize = 0;
        if (_xdgPopup != null) {
            _g.removePopup(this);
            _xdgPopup.destroy();
            _xdgPopup = null;
        }
        if (_xdgToplevel != null) {
            _xdgToplevel.destroy();
            _xdgToplevel = null;
        }
        if (_xdgSurface != null) {
            _xdgSurface.destroy();
            _xdgSurface = null;
        }
        if (_surface != null) {
            _g.unregister(_surface.getID());
            _surface.destroy();
            _surface = null;
        }
    }
    // Surface listener
    public boolean enter(int outputID) {
        // update configuration from output
        GraphicsDevice gd = _g.findDevice(outputID);
        if (gd != null)
            _config = gd.getDefaultConfiguration();
        return true;
    }
    public boolean leave(int outputID) { return true; }
    // XdgSurface listener
    public boolean xdgSurfaceConfigure(int serial) {
        return _xdgSurface.ackConfigure(serial);
    }
    // XdgToplevel listener
    public boolean xdgToplevelConfigure(int w, int h, int[] states) {
        // adjust to actual size..
        if (w > 0 && h > 0) {
            super.setSize(w, h);
        // ..or set original size (if any)..
        } else if (_origWidth > 0 && _origHeight > 0) {
            super.setSize(_origWidth, _origHeight);
        // ..or use a default.
        } else {
            super.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        // update display states
        _isActive = _isFullscreen = _isMaximized = _isResizing = false;
        _isFloating = true;
        for (int s : states) {
            if (XdgToplevel.STATE_ACTIVATED == s) _isActive = true;
            else if (XdgToplevel.STATE_FULLSCREEN == s) _isFullscreen = true;
            else if (XdgToplevel.STATE_MAXIMIZED == s) _isMaximized = true;
            else if (XdgToplevel.STATE_RESIZING == s) _isResizing = true;
            else _isFloating = false;
        }
        return true;
    }
    public boolean xdgToplevelClose() {
        sendEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        return true;
    }
    // XdgPopup listener
    public boolean xdgPopupConfigure(int x, int y, int w, int h) {
        // ignore zero sizes
        if (w>0 && h>0) {
            setLocation(x, y);
            super.setSize(w, h);
        }
        return true;
    }
    public boolean xdgPopupDone() {
        // We've been unmapped - clean up
        dispose();
        return true;
    }
    public boolean xdgPopupRepositioned(int token) { return true; }

    // ownership / lifecycle management
    public Window getOwner() { return _owner; }
    private void addOwned(Window w) {
        _log.info("addOwned("+w.getName()+")");
        synchronized(_owned) {
            if (!_owned.contains(w))
                _owned.add(w);
        }
    }
    private void remOwned(Window w) {
        _log.info("remOwned("+w.getName()+")");
        synchronized(_owned) {
            _owned.remove(w);
        }
    }
    public void dispose() {
        _log.info("Window:dispose()");
        setVisible(false);
        LinkedList<Window> copy;
        synchronized(_owned) {
            copy = new LinkedList<Window>(_owned);
        }
        for (Window w : copy)
            w.dispose();
        if (_owner != null) {
            _owner.remOwned(this);
            _owner = null;
        }
        sendEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSED));
    }

    // window event listeners
    public void addWindowListener(WindowListener l) {
        _listeners.add(l);
    }
    public void removeWindowListener(WindowListener l) {
        _listeners.remove(l);
    }
    private void sendEvent(WindowEvent w) {
        for (WindowListener l : _listeners) {
            if (w.getID() == WindowEvent.WINDOW_OPENED)
                l.windowOpened(w);
            else if (w.getID() == WindowEvent.WINDOW_CLOSED)
                l.windowClosed(w);
        }
        if (!w.isConsumed())
            processEvent(w);
    }

    // title - held here to avoid leaking Wayland stuff into sub-classes
    protected String getTitle() { return _title; }
    protected void setTitle(String title) {
        _log.info("Window:setTitle("+title+")");
        _title = title;
        repaint();
    }

    // adjust our size (but not less than minimum if set) to fit contents
    public void pack() {
        if (isMinimumSizeSet()) {
            // temporarily remove fixed size
            Dimension d = getMinimumSize();
            setMinimumSize(null);
            // get layout minimum
            Dimension l = getMinimumSize();
            // calc clamped minimum
            Dimension m = new Dimension((l._w > d._w) ? l._w : d._w, (l._h > d._h) ? l._h : d._h);
            // put stuff back
            setSize(m);
            setMinimumSize(m);
        } else
            setSize(getMinimumSize());
    }
    // intercept setSize/setBounds to inform Wayland
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (!isValid() && _xdgToplevel != null) {
            _xdgToplevel.setMinSize(w, h);
            _xdgToplevel.setMaxSize(w, h);
        }
    }
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (!isValid() && _xdgToplevel != null) {
            _xdgToplevel.setMinSize(w, h);
            _xdgToplevel.setMaxSize(w, h);
        }
    }
    // intercept isShowing as we are the end of the parent chain
    public boolean isShowing() {
        return isVisible();
    }
    // intercept setVisible to force validation and Wayland I/O
    public void setVisible(boolean v) {
        // no change?
        if (isVisible() == v)
            return;
        _log.info("Window:setVisible("+v+")");
        if (v) {
            // if we are a popup and have a size of 0,0, adjust to minimum before validating
            if (_isPopup && getWidth() == 0 && getHeight() == 0)
                setSize(getMinimumSize());
            validate();
            super.setVisible(v);
            toWayland();
            if (!_hasOpened) {
                _hasOpened = true;
                sendEvent(new WindowEvent(this, WindowEvent.WINDOW_OPENED));
            }
            repaint();
        } else {
            // hide all owned windows
            synchronized(_owned) {
                for (Window w : _owned)
                    w.setVisible(v);
            }
            fromWayland();
            super.setVisible(v);
        }
    }

    // intercept invalidate calls to queue a repaint
    public void invalidate() {
        if (!isValid())
            return;
        _log.info("Window:invalidate()");
        super.invalidate();
        repaint();
    }

    // intercept repaint calls, this is where we process them
    public void repaint() {
        if (isVisible()) {
            _log.info("Window:repaint()");
            _g.repaint(this);
        }
    }

    // get a Graphics context for drawing on this window
    public Graphics getGraphics() {
        if (_buffer != null)
            return new Graphics(_buffer.get(), getWidth(), getHeight(), getForeground(), getFont());
        return null;
    }

    // get a graphics configuration that describes stuff
    public GraphicsConfiguration getGraphicsConfiguration() {
        return _config;
    }

    // package-private fullscreen controls
    boolean setFullscreen(GraphicsDevice d) {
        if (isFullscreen())
            return true;
        _log.info("Window:setFullscreen()");
        return _xdgToplevel.setFullscreen(_g.findOutput(d));
    }
    boolean unsetFullscreen(GraphicsDevice d) {
        if (!isFullscreen())
            return true;
        _log.info("Window:unsetFullscreen()");
        return _xdgToplevel.unsetFullscreen();
    }
    // display state info
    public boolean isActive() { return _isActive; }

    // NB: not part of Swing API.. below here
    public boolean isMaximized() { return _isMaximized; }
    public boolean isFullscreen() { return _isFullscreen; }
    public boolean isResizing() { return _isResizing; }
    public boolean isFloating() { return _isFloating; }

    // Wayland-specific request interactive reposition / resize
    protected void reposition() {
        if (_xdgToplevel != null && isFloating()) {
            _g.reposition(_xdgToplevel);
        }
    }
    // map from SwingConstants directions to Wayland edges
    private static int[] _directionMap = {
        XdgToplevel.EDGE_NONE,
        XdgToplevel.EDGE_TOP,
        XdgToplevel.EDGE_TOP | XdgToplevel.EDGE_RIGHT,
        XdgToplevel.EDGE_RIGHT,
        XdgToplevel.EDGE_BOTTOM | XdgToplevel.EDGE_RIGHT,
        XdgToplevel.EDGE_BOTTOM,
        XdgToplevel.EDGE_BOTTOM | XdgToplevel.EDGE_LEFT,
        XdgToplevel.EDGE_LEFT,
        XdgToplevel.EDGE_TOP | XdgToplevel.EDGE_LEFT
    };
    protected void resize(int dir) {
        if (_xdgToplevel != null && isFloating()) {
            _g.resize(_xdgToplevel, _directionMap[dir]);
        }
    }
}
