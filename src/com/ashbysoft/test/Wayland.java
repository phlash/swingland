package com.ashbysoft.test;

import com.ashbysoft.wayland.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Wayland implements
    Callback.Listener,
    Display.Listener,
    Registry.Listener,
    Surface.Listener,
    XdgWmBase.Listener,
    XdgSurface.Listener,
    XdgToplevel.Listener {
    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private Surface _surface;
    private XdgWmBase _xdgWmBase;
    private XdgSurface _xdgSurface;
    private XdgToplevel _xdgToplevel;
    private Shm _shm;
    private int _width;
    private int _height;
    private int _poolsize;
    private ShmPool _shmpool;
    private Callback _frame;
    private Buffer _buffer;
    private Random _rand;
    public void run(String[] args) {
        System.out.println("---- Wayland test starts ----");
        _rand = new Random();
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
        _surface = new Surface(_display);
        _compositor.createSurface(_surface);
        _xdgSurface = new XdgSurface(_display);
        _xdgSurface.addListener(this);
        _xdgWmBase.getXdgSurface(_xdgSurface, _surface);
        _xdgToplevel = new XdgToplevel(_display);
        _xdgToplevel.addListener(this);
        _xdgSurface.getTopLevel(_xdgToplevel);
        _xdgToplevel.setTitle("Java wayland API!");
        _xdgToplevel.setAppID("com.ashbysoft.wayland.Test");
        _surface.commit();
        _display.roundtrip();
        _width = 640;
        _height = 480;
        _poolsize = 0;
        done(0);
        long start = System.currentTimeMillis();
        while (_display.dispatch()) {
            try { Thread.currentThread().sleep(10); } catch (Exception e) {}
            long now = System.currentTimeMillis();
            if (now > start+2000)
                break;
        }
        _display.close();
        System.out.println("---- Wayland test done ----");
    }
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
    public boolean done(int serial) {
        _frame = new Callback();
        _frame.addListener(this);
        _surface.frame(_frame);
        render();
        return true;
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
    public boolean enter(int outputID) { return true; }
    public boolean leave(int outputID) { return true; }
    public boolean ping(int serial) { return _xdgWmBase.pong(serial); }
    public boolean xdgSurfaceConfigure(int serial) { return _xdgSurface.ackConfigure(serial); }
    public boolean xdgToplevelConfigure(int w, int h, int[] states) { _width = w; _height = h; return true; }
    public boolean xdgToplevelClose() { return true; }
}