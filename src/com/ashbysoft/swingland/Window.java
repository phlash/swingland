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

import java.util.LinkedList;

public class Window extends Container implements
    Surface.Listener,
    XdgSurface.Listener,
    XdgToplevel.Listener,
    XdgPopup.Listener {
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT= 480;

    // Wayland objects instantiated per-window
    private WaylandGlobals _g;
    private Surface _surface;
    private XdgSurface _xdgSurface;
    private XdgToplevel _xdgToplevel;
    private XdgPopup _xdgPopup;
    private int _poolsize;
    private ShmPool _shmpool;
    private Buffer _buffer;
    private Surface _cursorSurface;
    private int _lastCursor;
    // original size (if any), used during configure callback
    private int _origWidth;
    private int _origHeight;
    // window ownership hierarchy, affects lifecycle / visibility methods
    private Window _owner;
    private LinkedList<Window> _owned;
    // popup window (short-lived, eg: menus, tooltips)
    private boolean _isPopup;
    // title text - here as it's passed to Wayland
    private String _title;

    public Window() {
        _log.info("Window:<init>()");
        init();
    }
    protected Window(Window owner) { this(owner, false); }
    protected Window(Window owner, boolean isPopup) {
        _log.info("Window:<init>("+owner.getName()+","+isPopup+")");
        assert(owner != null);
        _owner = owner;
        _owner.addOwned(this);
        _isPopup = isPopup;
        init();
    }
    private void init() {
        _owned = new LinkedList<Window>();
        // grab a reference to Wayland
        _g = WaylandGlobals.instance();
        // default rendering properties
        setBackground(Color.LIGHT_GRAY);
        setForeground(Color.BLACK);
        setFont(Font.getFont(Font.MONOSPACED));
        setCursor(Cursor.getDefaultCursor());
        _lastCursor = -1;
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
    // package-private cursor surface creation for Wayland
    Surface getCursorSurface() {
        if (null == _cursorSurface) {
            _log.info("getCursorSurface()");
            _cursorSurface = new Surface(_g.display());
            _g.compositor().createSurface(_cursorSurface);
        }
        return _cursorSurface;
    }
    // 
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
        _cursorSurface.attach(buffer, 0, 0);
        _cursorSurface.damageBuffer(0, 0, d._w, d._h);
        _cursorSurface.commit();
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
            // minimum positioner settings required..
            positioner.setSize(getWidth(), getHeight());
            positioner.setAnchorRect(_owner.getX(), _owner.getY(), _owner.getWidth(), _owner.getHeight());
        }
        _surface = new Surface(_g.display());
        _surface.addListener(this);
        _g.compositor().createSurface(_surface);
        _xdgSurface = new XdgSurface(_g.display());
        _xdgSurface.addListener(this);
        _g.xdgWmBase().getXdgSurface(_xdgSurface, _surface);
        // if requested, create a popup rather than a top level window
        if (_isPopup) {
            _xdgPopup = new XdgPopup(_g.display());
            _xdgPopup.addListener(this);
            _xdgSurface.getPopup(_xdgPopup, _owner._xdgSurface, positioner);
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
        _g.register(_surface.getID(), this);
        _g.display().roundtrip();
        repaint();
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
    public boolean enter(int outputID) { return true; }
    public boolean leave(int outputID) { return true; }
    // XdgSurface listener
    public boolean xdgSurfaceConfigure(int serial) {
        return _xdgSurface.ackConfigure(serial);
    }
    // XdgToplevel listener
    public boolean xdgToplevelConfigure(int w, int h, int[] states) {
        // adjust to actual size..
        if (w > 0 && h > 0) {
            setSize(w, h);
        // ..or set original size (if any)..
        } else if (_origWidth > 0 && _origHeight > 0) {
            setSize(_origWidth, _origHeight);
        // ..or use a default.
        } else {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        repaint();
        return true;
    }
    public boolean xdgToplevelClose() {
        // XXX:TODO: process disposeOnClose setting..
        return true;
    }
    // XdgPopup listener
    public boolean xdgPopupConfigure(int x, int y, int w, int h) {
        // ignore zero sizes
        if (w>0 && h>0) {
            setLocation(x, y);
            setSize(w, h);
            repaint();
        }
        return true;
    }
    public boolean xdgPopupDone() { return true; }
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
        if (_owner != null)
            _owner.remOwned(this);
        LinkedList<Window> copy;
        synchronized(_owned) {
            copy = new LinkedList<Window>(_owned);
        }
        for (Window w : copy)
            w.dispose();
        fromWayland();
    }

    protected String getTitle() { return _title; }
    protected void setTitle(String title) {
        _log.info("Window:setTitle("+title+")");
        _title = title;
        repaint();
    }

    // intercept setVisible to force validation and Wayland I/O
    public void setVisible(boolean v) {
        // no change?
        if (isVisible() == v)
            return;
        _log.info("Window:setVisible("+v+")");
        if (v) {
            validate();
            super.setVisible(v);
            toWayland();
        } else {
            // hide all owned windows
            synchronized(_owned) {
                for (Window w : _owned)
                    w.setVisible(v);
            }
            super.setVisible(v);
            fromWayland();
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
}
