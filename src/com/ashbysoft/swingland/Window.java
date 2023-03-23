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
    XdgToplevel.Listener {

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
        private int _windowCount;
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
            _windowCount = 1;
            _uiThread = new Thread(this);
            // We hold the application active even if main exits
            _uiThread.setDaemon(false);
            _uiThread.start();
        }
        public void incref() {
            _windowCount += 1;
        }
        public void decref() {
            _windowCount -= 1;
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
        // XXX:TODO: accumulate shift state into 'typed' events
        public boolean keymap(int format, int fd, int size) { return true; }
        public boolean keyboardEnter(int serial, int surface, int[] keys) { _keyboardWindow = surface; return true; }
        public boolean keyboardLeave(int serial, int surface) { return true; }
        public boolean key(int serial, int time, int keyCode, int state) {
            Window w;
            synchronized(_windows) {
                w = _windows.get(_keyboardWindow);
            }
            if (w != null) {
                KeyEvent e = new KeyEvent(this, state, keyCode);
                w.dispatchEvent(e);
            }
            return true;
        }
        public boolean modifiers(int serial, int depressed, int latched, int locked, int group) { return true; }
        public boolean repeat(int rate, int delay) { return true; }
        // Pointer.Listener
        // XXX:TODO: accumulate press/release state per button into 'clicked' events, save x/y for button events
        public boolean pointerEnter(int serial, int surface, int x, int y) { _pointerWindow = surface; return true; }
        public boolean pointerLeave(int serial, int surface) { return true; }
        public boolean pointerMove(int time, int x, int y) {
            Window w;
            synchronized(_windows) {
                w = _windows.get(_pointerWindow);
            }
            if (w != null) {
                MouseEvent e = new MouseEvent(this, MouseEvent.MOUSE_MOVE, x>>8, y>>8, -1, -1);
                w.dispatchEvent(e);
            }
            return true;
        }
        public boolean pointerButton(int serial, int time, int button, int state) {
            Window w;
            synchronized(_windows) {
                w = _windows.get(_pointerWindow);
            }
            if (w != null) {
                MouseEvent e = new MouseEvent(this, MouseEvent.MOUSE_BUTTON, -1, -1, button, state);
                w.dispatchEvent(e);
            }
            return true;
        }

        public void repaint(Window w) {
            synchronized(_repaints) {
                // coalesce repaints for the same window
                if (!_repaints.contains(w))
                    _repaints.add(w);
            }
        }
        public void run() 
        {
            // process wayland responses, repaint requests until no more windows
            while (_windowCount > 0) {
                _display.dispatch();
                while (_repaints.size() > 0) {
                    synchronized(_repaints) {
                        Window w = _repaints.remove();
                        w.render();
                    }
                }
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
    private int _poolsize;
    private ShmPool _shmpool;
    private Buffer _buffer;
    private Callback _frame;

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
            Graphics g = new Graphics(_buffer.get(), getWidth(), getHeight());
            paint(g);
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        _surface.commit();
    }
    private void toWayland() {
        // first time?
        if (null == _globals) {
            _globals = new WaylandGlobals();
        } else {
            _globals.incref();
        }
        // hook us up to the real world!
        _surface = new Surface(_globals.display());
        _surface.addListener(this);
        _globals.compositor().createSurface(_surface);
        _xdgSurface = new XdgSurface(_globals.display());
        _xdgSurface.addListener(this);
        _globals.xdgWmBase().getXdgSurface(_xdgSurface, _surface);
        _xdgToplevel = new XdgToplevel(_globals.display());
        _xdgToplevel.addListener(this);
        _xdgSurface.getTopLevel(_xdgToplevel);
        _xdgToplevel.setTitle(getName());
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
        if (_globals != null)
            _globals.decref();
    }
    public boolean enter(int outputID) { return true; }
    public boolean leave(int outputID) { return true; }
    public boolean xdgSurfaceConfigure(int serial) { return _xdgSurface.ackConfigure(serial); }
    public boolean xdgToplevelConfigure(int w, int h, int[] states) {
        // ignore zero sizes
        if (w>0 && h>0) {
            setSize(w, h);
            repaint();
        }
        return true;
    }
    public boolean xdgToplevelClose() {
        // XXX:TODO: process disposeOnClose setting..
        return true;
    }
    public boolean done(int serial) { return true; }

    public void dispose() {
        fromWayland();
    }

    // intercept setVisible to force validation and Wayland I/O
    public void setVisible(boolean v) {
        if (v) {
            validate();
            super.setVisible(v);
            toWayland();
        } else {
            super.setVisible(v);
            fromWayland();
        }
    }

    // intercept repaint calls, this is where we process them
    public void repaint() {
        _globals.repaint(this);
    }

    // paint ourselves (background), then delegate to container
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        _log.info("Window:paint");
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }
}