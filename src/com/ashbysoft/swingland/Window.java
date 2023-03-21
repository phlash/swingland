package com.ashbysoft.swingland;

// All renderable objects (Frame, Dialog, etc.) are based on Window. So this
// is where we choose to connect across to Wayland, holding the drawing surface
// and performing rendering operations.

import com.ashbysoft.wayland.*;
import java.util.LinkedList;

public class Window extends Container implements
    Surface.Listener,
    XdgSurface.Listener,
    XdgToplevel.Listener {

    // container for all Wayland global objects and UI thread holder
    private class WaylandGlobals implements
        Runnable,
        Registry.Listener,
        XdgWmBase.Listener {

        private Display _display;
        private Registry _registry;
        private Compositor _compositor;
        private XdgWmBase _xdgWmBase;
        private Shm _shm;
        private Thread _uiThread;
        private LinkedList<Window> _repaints;
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
            _uiThread = new Thread(this);
            // We hold the application active even if main exits
            _uiThread.setDaemon(false);
            _uiThread.start();
        }
        public Display display() { return _display; }
        public Compositor compositor() { return _compositor; }
        public XdgWmBase xdgWmBase() { return _xdgWmBase; }
        public Shm shm() { return _shm; }
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
            }
            return true;
        }
        public boolean remove(int name) { return true; }
        public boolean ping(int serial) {
            return _xdgWmBase.pong(serial);
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
            // process wayland responses, repaint requests
            while (true) {
                _display.dispatch();
                while (_repaints.size() > 0) {
                    synchronized(_repaints) {
                        Window w = _repaints.remove();
                        w.render();
                    }
                }
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
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
            _surface.destroy();
            _surface = null;
        }
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

    public void dispose() {}

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