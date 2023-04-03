package com.ashbysoft.test;

import com.ashbysoft.wayland.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Wayland implements Display.Listener, Registry.Listener, XdgWmBase.Listener {

    private class Toplevel implements
        Callback.Listener,
        Surface.Listener,
        XdgSurface.Listener,
        XdgToplevel.Listener {
        private Random _rand;
        private Surface _surface;
        private XdgSurface _xdgSurface;
        private XdgToplevel _xdgToplevel;
        private ShmPool _shmpool;
        private Buffer _buffer;
        private Callback _frame;
        private int _width;
        private int _height;
        private int _poolsize;
        public Toplevel() {
            _rand = new Random();
            _surface = new Surface(_display);
            _surface.addListener(this);
            _compositor.createSurface(_surface);
            _xdgSurface = new XdgSurface(_display);
            _xdgSurface.addListener(this);
            _xdgWmBase.getXdgSurface(_xdgSurface, _surface);
            _xdgToplevel = new XdgToplevel(_display);
            _xdgToplevel.addListener(this);
            _xdgSurface.getTopLevel(_xdgToplevel);
            _xdgToplevel.setTitle("Java wayland API!");
            _xdgToplevel.setAppID("com.ashbysoft.test.Wayland");
            _surface.commit();
            _display.roundtrip();
            _width = 640;
            _height = 480;
            _poolsize = 0;
            done(0);
        }
        public XdgSurface getXdgSurface() { return _xdgSurface; }
        public boolean enter(int outputID) { return true; }
        public boolean leave(int outputID) { return true; }
        public boolean done(int serial) {
            _frame = new Callback(_display);
            _frame.addListener(this);
            _surface.frame(_frame);
            render();
            return true;
        }
        public boolean xdgSurfaceConfigure(int serial) { return _xdgSurface.ackConfigure(serial); }
        public boolean xdgToplevelConfigure(int w, int h, int[] states) { if (w>0 && h>0) { _width = w; _height = h; } return true; }
        public boolean xdgToplevelClose() { return true; }
        private void render() {
            // new buffer required?
            int nextsize = _width * _height * 4;
            if (nextsize != _poolsize) {
                if (_buffer != null)
                    _buffer.destroy();
                if (_shmpool != null)
                    _shmpool.destroy();
                _poolsize = nextsize;
                _shmpool = _shm.createPool(_poolsize);
                _buffer = _shmpool.createBuffer(0, _width, _height, _width*4, 0);
            }
            ByteBuffer pixels = _buffer.get();
            pixels.rewind();
            for (int y=0; y<_height; y++)
                for (int x=0; x<_width; x++)
                    pixels.putInt(0xff000000 | (_rand.nextInt() & 0x00ffffff));
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            _surface.commit();
        }
        public void destroy() {
            if (_buffer != null) _buffer.destroy();
            if (_shmpool != null) _shmpool.destroy();
            _xdgToplevel.destroy();
            _xdgSurface.destroy();
            _surface.destroy();
        }
    }
    private class Popup implements Callback.Listener, XdgSurface.Listener, XdgPopup.Listener {
        private Surface _surface;
        private XdgSurface _xdgSurface;
        private XdgPopup _xdgPopup;
        private ShmPool _shmpool;
        private Buffer _buffer;
        private Callback _frame;
        private int _width;
        private int _height;
        private int _poolsize;
        private int _count;
        public Popup(XdgSurface parent) {
            _width = 200;
            _height = 100;
            _count = 0;
            // MUST set at least size and anchor rect
            Positioner positioner = new Positioner(_display);
            _xdgWmBase.createPositioner(positioner);
            positioner.setSize(_width, _height);
            positioner.setAnchorRect(50, 50, _width, _height);
            _surface = new Surface(_display);
            _compositor.createSurface(_surface);
            _xdgSurface = new XdgSurface(_display);
            _xdgSurface.addListener(this);
            _xdgWmBase.getXdgSurface(_xdgSurface, _surface);
            _xdgPopup = new XdgPopup(_display);
            _xdgPopup.addListener(this);
            _xdgSurface.getPopup(_xdgPopup, parent, positioner);
            _surface.commit();
            _display.roundtrip();
            positioner.destroy();
            _poolsize = 0;
            done(0);
        }
        public boolean done(int serial) {
            _frame = new Callback(_display);
            _frame.addListener(this);
            _surface.frame(_frame);
            render();
            return true;
        }
        public boolean xdgSurfaceConfigure(int serial) { return _xdgSurface.ackConfigure(serial); }
        public boolean xdgPopupConfigure(int x, int y, int w, int h) { if (w>0 && h>0) { _width = w; _height = h; } return true; }
        public boolean xdgPopupDone() { return true; }
        public boolean xdgPopupRepositioned(int token) { return true; }
        private void render() {
            // new buffer required?
            int nextsize = _width * _height * 4;
            if (nextsize != _poolsize) {
                if (_buffer != null)
                    _buffer.destroy();
                if (_shmpool != null)
                    _shmpool.destroy();
                _poolsize = nextsize;
                _shmpool = _shm.createPool(_poolsize);
                _buffer = _shmpool.createBuffer(0, _width, _height, _width*4, 0);
            }
            ByteBuffer pixels = _buffer.get();
            pixels.rewind();
            for (int y=0; y<_height; y++)
                for (int x=0; x<_width; x++)
                    pixels.putInt(0xff000000 | (_count++ & 0x00ffffff));
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            _surface.commit();
        }
        public void destroy() {
            if (_buffer != null) _buffer.destroy();
            if (_shmpool != null) _shmpool.destroy();
            _xdgPopup.destroy();
            _xdgSurface.destroy();
            _surface.destroy();
        }
    }

    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private XdgWmBase _xdgWmBase;
    private Shm _shm;
    public void run(String[] args) {
        System.out.println("---- Wayland test starts ----");
        _display = new Display();
        _display.addListener(this);
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        _display.roundtrip();
        if (null == _compositor)
            throw new RuntimeException("oops: did not see a compositor!");
        if (null == _shm)
            throw new RuntimeException("oops: did not see an shm");
        if (null == _xdgWmBase)
            throw new RuntimeException("oops: did not see an xdg_wm_base");
        _xdgWmBase.addListener(this);
        _display.roundtrip();
        Toplevel toplevel = new Toplevel();
        _display.roundtrip();
        Popup popup = new Popup(toplevel.getXdgSurface());
        long start = System.currentTimeMillis();
        while (_display.dispatch()) {
            try { Thread.sleep(10); } catch (Exception e) {}
            long now = System.currentTimeMillis();
            if (now > start+2000)
                break;
        }
        popup.destroy();
        toplevel.destroy();
        _display.close();
        System.out.println("---- Wayland test done ----");
    }
    public void error(int id, int code, String msg) {
        System.err.println("OOPS: object="+id+" code="+code+" message="+msg);
    }
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
    public boolean remove(int name) {
        return true;
    }
    public boolean ping(int serial) { return _xdgWmBase.pong(serial); }
    public boolean popupConfigure(int x, int y, int w, int h) { return true; }
    public boolean popupDone() { return true; }
    public boolean popupRepositioned(int token) { return true; }
}
