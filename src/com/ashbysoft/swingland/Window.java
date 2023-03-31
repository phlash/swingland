package com.ashbysoft.swingland;

// All renderable objects (Frame, Dialog, etc.) are based on Window. So this
// is where we choose to connect across to Wayland, holding the drawing surface
// and performing rendering operations.

import com.ashbysoft.wayland.*;
import com.ashbysoft.swingland.event.*;
import java.util.LinkedList;
import java.util.HashMap;

public class Window extends Container implements
    Surface.Listener,
    XdgSurface.Listener,
    XdgToplevel.Listener,
    XdgPopup.Listener {
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT= 480;

    // container for all Wayland global objects and UI thread holder
    private class WaylandGlobals implements
        Runnable,
        Registry.Listener,
        XdgWmBase.Listener,
        Seat.Listener,
        Keyboard.Listener,
        Pointer.Listener {

        private Display _display;
        private Registry _registry;
        private Compositor _compositor;
        private XdgWmBase _xdgWmBase;
        private Shm _shm;
        private Seat _seat;
        private String _seatName;
        private Keyboard _keyboard;
        private Pointer _pointer;
        private Thread _uiThread;
        private LinkedList<Window> _repaints;
        private HashMap<Integer, Window> _windows;
        private int _keyboardWindow;
        private int _pointerWindow;

        public WaylandGlobals() {
            _display = new Display();
            _registry = new Registry(_display);
            _registry.addListener(this);
            _display.getRegistry(_registry);
            _display.roundtrip();
            if (null == _compositor || null == _xdgWmBase || null == _shm) {
                String oops = "missing a global object in Wayland: compositor="+_compositor+" xdgWmBase="+_xdgWmBase+" shm="+_shm;
                _log.error(oops);
                throw new RuntimeException(oops);
            }
            _xdgWmBase.addListener(this);
            _repaints = new LinkedList<Window>();
            _windows = new HashMap<Integer, Window>();
            _uiThread = new Thread(this);
            // We hold the application active even if main exits
            _uiThread.setDaemon(false);
            _uiThread.start();
        }
        public Display display() { return _display; }
        public Compositor compositor() { return _compositor; }
        public XdgWmBase xdgWmBase() { return _xdgWmBase; }
        public Shm shm() { return _shm; }
        public void register(int id, Window w) {
            synchronized(_windows) {
                _windows.put(id, w);
            }
        }
        public void unregister(int id) {
            synchronized(_windows) {
                _windows.remove(id);
            }
        }
        // Registry.Listener
        public boolean global(int name, String iface, int version) {
            if (iface.equals("wl_compositor")) {
                _compositor = new Compositor(_display);
                _registry.bind(name, iface, version, _compositor);
            } else if (iface.equals("wl_shm")) {
                _shm = new Shm(_display);
                _registry.bind(name, iface, version, _shm);
            } else if (iface.equals("xdg_wm_base")) {
                _xdgWmBase = new XdgWmBase(_display);
                _registry.bind(name, iface, version, _xdgWmBase);
            } else if (iface.equals("wl_seat")) {
                _seat = new Seat(_display);
                _seat.addListener(this);
                _registry.bind(name, iface, version, _seat);
            }
            return true;
        }
        public boolean remove(int name) { return true; }
        // XdgWmBase.Listener
        public boolean ping(int serial) {
            return _xdgWmBase.pong(serial);
        }
        // Seat.Listener
        public boolean seatCapabilities(int caps) {
            if ((caps & Seat.Listener.KEYBOARD) != 0) {
                _keyboard = new Keyboard(_display);
                _keyboard.addListener(this);
                _seat.getKeyboard(_keyboard);
            }
            if ((caps & Seat.Listener.POINTER) != 0) {
                _pointer = new Pointer(_display);
                _pointer.addListener(this);
                _seat.getPointer(_pointer);
            }
            return true;
        }
        public boolean seatName(String name) { _seatName = name; return true; }
        // Keyboard.Listener
        private Keymap _keymap;
        public boolean keymap(int format, int fd, int size) {
            // XXX:TODO: use a real keymap once we can read the fd..
            _keymap = new DefaultKeymap();
            return true;
        }
        public boolean keyboardEnter(int serial, int surface, int[] keys) {
            _keyboardWindow = surface;
            for (int k : keys)
                key(serial, 0, k, KeyEvent.KEY_PRESSED);
            return true;
        }
        public boolean keyboardLeave(int serial, int surface) { return true; }
        public boolean key(int serial, int time, int keyCode, int state) {
            Window w;
            synchronized(_windows) {
                w = _windows.get(_keyboardWindow);
            }
            if (w != null) {
                KeyEvent e = new KeyEvent(this, state, keyCode, '-');
                w.dispatchEvent(e);
                if (KeyEvent.KEY_PRESSED == state) {
                    e = _keymap.mapCode(keyCode);
                    if (e != null)
                        w.dispatchEvent(e);
                }
            }
            return true;
        }
        public boolean modifiers(int serial, int depressed, int latched, int locked, int group) {
            _keymap.modifiers(depressed, latched, locked, group);
            return true;
        }
        public boolean repeat(int rate, int delay) { return true; } // XXX:TODO: key repeats
        // Pointer.Listener
        private int _pointerX;
        private int _pointerY;
        private boolean pointerSend(MouseEvent m) {
            Window w;
            synchronized(_windows) {
                w = _windows.get(_pointerWindow);
            }
            if (w != null) {
                w.dispatchEvent(m);
            }
            return true;
        }
        public boolean pointerEnter(int serial, int surface, int x, int y) {
            _pointerWindow = surface;
            return pointerMove(0, x, y);
        }
        public boolean pointerLeave(int serial, int surface) {
            return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_EXITED, _pointerX >> 8, _pointerY >> 8, -1, -1));
        }
        public boolean pointerMove(int time, int x, int y) {
            _pointerX = x;
            _pointerY = y;
            return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_MOVE, _pointerX >> 8, _pointerY >> 8, -1, -1));
        }
        public boolean pointerButton(int serial, int time, int button, int state) {
            return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_BUTTON, _pointerX >> 8, _pointerY >> 8, button, state));
        }

        // repaint request queue
        public void repaint(Window w) {
            synchronized(_repaints) {
                // coalesce repaints for the same window
                if (!_repaints.contains(w))
                    _repaints.add(w);
            }
        }

        // UI thread
        public void run() 
        {
            // we run until:
            // - we have seen at least one window
            // - there are now zero registered windows
            boolean canExit = false;
            while (true) {
                _display.dispatch();
                if (_repaints.size() > 0) {
                    Window w;
                    synchronized(_repaints) {
                        w = _repaints.remove();
                    }
                    if (w != null)
                        w.render();
                }
                int count;
                synchronized(_windows) {
                    count = _windows.size();
                }
                if (count > 0)
                    canExit = true;
                else if (canExit)
                    break;
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
            _display.close();
        }
    }
    private static WaylandGlobals _globals;

    // Wayland objects instantiated per-window
    private Surface _surface;
    private XdgSurface _xdgSurface;
    private XdgToplevel _xdgToplevel;
    private XdgPopup _xdgPopup;
    private int _poolsize;
    private ShmPool _shmpool;
    private Buffer _buffer;
    private Callback _frame;
    // original size (if any), used during configure callback
    private int _origWidth;
    private int _origHeight;
    // window ownership hierarchy, affects lifecycle / visibility methods
    private Window _owner;
    private LinkedList<Window> _owned = new LinkedList<Window>();
    // popup window (short-lived, eg: menus, tooltips)
    private boolean _isPopup;
    // title text - here as it's passed to Wayland
    private String _title;

    public Window() {}
    protected Window(Window owner) { this(owner, false); }
    protected Window(Window owner, boolean isPopup) {
        assert(owner != null);
        _owner = owner;
        _owner.addOwned(this);
        _isPopup = isPopup;
    }

    private void render() {
        // check Wayland is done with previous buffer (if any)
        if (_frame != null && !_frame.done()) {
            _log.error("render overrun");
            return;
        }
        _frame = new Callback();
        _surface.frame(_frame);
        // buffer size changed?
        int psize = getWidth() * getHeight() * 4;
        if (psize != _poolsize) {
            // drop existing pool if any
            if (_buffer != null)
                _buffer.destroy();
            if (_shmpool != null)
                _shmpool.destroy();
            _poolsize = psize;
            _shmpool = _globals.shm().createPool(_poolsize);
            _buffer = _shmpool.createBuffer(0, getWidth(), getHeight(), getWidth() * 4, 0);
        }
        if (_buffer != null) {
            validate();
            paint(getGraphics());
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        _surface.commit();
    }
    private void toWayland() {
        // first time?
        if (null == _globals) {
            _globals = new WaylandGlobals();
        }
        // save original size..
        _origWidth = getWidth();
        _origHeight= getHeight();
        // hook us up to the real world!
        Positioner positioner = null;
        if (_isPopup) {
            // NB: we do this /before/ any surface stuff, or Sway breaks :(
            positioner = new Positioner(_globals.display());
            _globals.xdgWmBase().createPositioner(positioner);
            // minimum positioner settings required..
            positioner.setSize(getWidth(), getHeight());
            positioner.setAnchorRect(_owner.getX(), _owner.getY(), _owner.getWidth(), _owner.getHeight());
        }
        _surface = new Surface(_globals.display());
        _surface.addListener(this);
        _globals.compositor().createSurface(_surface);
        _xdgSurface = new XdgSurface(_globals.display());
        _xdgSurface.addListener(this);
        _globals.xdgWmBase().getXdgSurface(_xdgSurface, _surface);
        // if requested, create a popup rather than a top level window
        if (_isPopup) {
            _xdgPopup = new XdgPopup(_globals.display());
            _xdgPopup.addListener(this);
            _xdgSurface.getPopup(_xdgPopup, _owner._xdgSurface, positioner);
            positioner.destroy();
        } else {
            _xdgToplevel = new XdgToplevel(_globals.display());
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
        _globals.register(_surface.getID(), this);
        _globals.display().roundtrip();
        repaint();
    }
    private void fromWayland() {
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
            _globals.unregister(_surface.getID());
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
        synchronized(_owned) {
            if (!_owned.contains(w))
                _owned.add(w);
        }
    }
    private void remOwned(Window w) {
        synchronized(_owned) {
            _owned.remove(w);
        }
    }
    public void dispose() {
        _log.info("dispose");
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
        _log.info("setTitle:"+title);
        _title = title;
        repaint();
    }

    // intercept setVisible to force validation and Wayland I/O
    public void setVisible(boolean v) {
        // no change?
        if (isVisible() == v)
            return;
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
        super.invalidate();
        repaint();
    }

    // intercept repaint calls, this is where we process them
    public void repaint() {
        if (isVisible())
            _globals.repaint(this);
    }

    // get a Graphics context for drawing on this window
    public Graphics getGraphics() {
        if (_buffer != null)
            return new Graphics(_buffer.get(), getWidth(), getHeight());
        return null;
    }
}
