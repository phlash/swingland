package com.ashbysoft.test;

import com.ashbysoft.wayland.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Wayland implements Display.Listener, Registry.Listener, XdgWmBase.Listener, Pointer.Listener {

    private class Cursor {
        // fixed cursor, 16x16 left arrow, hotspot at 0,0
        private static final short[] _cursor = {
            (short)0b1111111111000000,
            (short)0b1111111100000000,
            (short)0b1111111000000000,
            (short)0b1111110000000000,
            (short)0b1111111000000000,
            (short)0b1111001100000000,
            (short)0b1110000110000000,
            (short)0b1100000011000000,
            (short)0b1000000001100000,
            (short)0b1000000000110000,
            (short)0b0000000000011000,
            (short)0b0000000000001100,
            (short)0b0000000000000110,
            (short)0b0000000000000011,
            (short)0b0000000000000001,
            (short)0b0000000000000000,
        };
        private static final int _hx = 0;
        private static final int _hy = 0;
        private Surface _surface;
        private ShmPool _shmpool;
        private Buffer _buffer;
        public Cursor() {
            // create a cursor and attach to our pointer..
            _surface = new Surface(_display);
            _compositor.createSurface(_surface);
            _shmpool = _shm.createPool(_cursor.length*16*4);
            _buffer = _shmpool.createBuffer(0, 16, 16, 16*4, 0);
            ByteBuffer pixels = _buffer.get();
            pixels.rewind();
            for (int y=0; y < _cursor.length; y += 1) {
                for (int x=0; x < 16; x+= 1) {
                    short m = (short)(1 << (15-x));
                    if ((_cursor[y] & m) > 0)
                        pixels.putInt(0xffffffff);
                    else
                        pixels.putInt(0);
                }
            }
        }
        public void attach(Pointer pointer, int serial) {
            pointer.setCursor(serial, _surface, _hx, _hy);
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            _surface.commit();
        }
        public void destroy() {
            if (_buffer != null) {
                _buffer.destroy();
                _buffer = null;
            }
            if (_shmpool != null) {
                _shmpool.destroy();
                _shmpool = null;
            }
            if (_surface != null) {
                _surface.destroy();
                _surface = null;
            }
        }
    }
    private class XdgCommon implements
        Callback.Listener,
        Surface.Listener,
        XdgSurface.Listener {
        protected Surface _surface;
        protected XdgSurface _xdgSurface;
        protected ShmPool _shmpool;
        protected Buffer _buffer;
        protected Callback _frame;
        protected int _width;
        protected int _height;
        protected int _poolsize;
        protected int _frames;

        protected XdgCommon(int w, int h) {
            _surface = new Surface(_display);
            _surface.addListener(this);
            _compositor.createSurface(_surface);
            _xdgSurface = new XdgSurface(_display);
            _xdgSurface.addListener(this);
            _xdgWmBase.getXdgSurface(_xdgSurface, _surface);
            _width = w;
            _height = h;
            _poolsize = 0;
            _frames = 0;
        }
        public boolean enter(int outputID) { return true; }
        public boolean leave(int outputID) { return true; }
        public boolean done(int serial) {
            _frame = new Callback(_display);
            _frame.addListener(this);
            _surface.frame(_frame);
            render();
            _frames += 1;
            return true;
        }
        public boolean xdgSurfaceConfigure(int serial) { return _xdgSurface.ackConfigure(serial); }
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
                    pixels.putInt(getPixel());
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            _surface.commit();
        }
        protected int getPixel() { return 0xff000000; }
    }
    private class Toplevel extends XdgCommon implements XdgToplevel.Listener {
        private Random _rand;
        private XdgToplevel _xdgToplevel;
        public Toplevel() {
            super(640, 480);
            _xdgToplevel = new XdgToplevel(_display);
            _xdgToplevel.addListener(this);
            _xdgSurface.getTopLevel(_xdgToplevel);
            _xdgToplevel.setTitle("Java wayland API!");
            _xdgToplevel.setAppID("com.ashbysoft.test.Wayland");
            _rand = new Random();
            _surface.commit();
            _display.roundtrip();
            done(0);
        }
        public XdgSurface getXdgSurface() { return _xdgSurface; }
        public boolean xdgToplevelConfigure(int w, int h, int[] states) { if (w>0 && h>0) { _width = w; _height = h; } return true; }
        public boolean xdgToplevelClose() { return true; }
        public int destroy() {
            if (_buffer != null) _buffer.destroy();
            if (_shmpool != null) _shmpool.destroy();
            _xdgToplevel.destroy();
            _xdgSurface.destroy();
            _surface.destroy();
            return _frames;
        }
        protected int getPixel() { return (_rand.nextInt() & 0x00ffffff) | 0xff000000; }
    }
    private class Popup extends XdgCommon implements XdgPopup.Listener {
        private XdgPopup _xdgPopup;
        private int _count;
        private static final int _w = 200;
        private static final int _h = 100;
        private Positioner _positioner = createPositioner(_w, _h);

        public Popup(XdgSurface parent) {
            super(_w, _h);
            _xdgPopup = new XdgPopup(_display);
            _xdgPopup.addListener(this);
            _xdgSurface.getPopup(_xdgPopup, parent, _positioner);
            _surface.commit();
            _display.roundtrip();
            _positioner.destroy();
            _count = 0;
            done(0);
        }
        private Positioner createPositioner(int w, int h) {
            // MUST set at least size and anchor rect
            Positioner p = new Positioner(_display);
            _xdgWmBase.createPositioner(p);
            p.setSize(_width, _height);
            p.setAnchorRect(50, 50, _width, _height);
            return p;
        }
        public boolean xdgPopupConfigure(int x, int y, int w, int h) { if (w>0 && h>0) { _width = w; _height = h; } return true; }
        public boolean xdgPopupDone() { return true; }
        public boolean xdgPopupRepositioned(int token) { return true; }
        public int destroy() {
            if (_buffer != null) _buffer.destroy();
            if (_shmpool != null) _shmpool.destroy();
            _xdgPopup.destroy();
            _xdgSurface.destroy();
            _surface.destroy();
            return _frames;
        }
        protected int getPixel() { return 0xff000000 | (_count++ & 0x00ffffff); }
    }

    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private XdgWmBase _xdgWmBase;
    private Shm _shm;
    private Seat _seat;
    private Pointer _pointer;
    private Cursor _cursor;
    public void run(String[] args) {
        int timeout = 2000;
        for (String arg : args) {
            if (arg.startsWith("t:")) {
                try { timeout = Integer.parseInt(arg.substring(2)); } catch (NumberFormatException e) {}
            }
        }
        System.out.println("---- Wayland test starts ("+timeout+"ms) ----");
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
        if (_seat != null) {
            _cursor = new Cursor();
            _pointer = new Pointer(_display);
            _pointer.addListener(this);
            _seat.getPointer(_pointer);
        }
        _display.roundtrip();
        Toplevel toplevel = new Toplevel();
        _display.roundtrip();
        Popup popup = new Popup(toplevel.getXdgSurface());
        long start = System.currentTimeMillis();
        long now = 0;
        while (_display.dispatch()) {
            try { Thread.sleep(10); } catch (Exception e) {}
            now = System.currentTimeMillis();
            if (now > start+timeout)
                break;
        }
        int f2 = (popup.destroy()*1000)/(int)(now-start);
        int f1 = (toplevel.destroy()*1000)/(int)(now-start);
        if (_pointer != null) _pointer.release();
        if (_cursor != null) _cursor.destroy();;
        _display.close();
        System.out.println("---- Wayland test done (fps:"+f1+"/"+f2+") ----");
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
        } else if (iface.equals("wl_seat")) {
            _seat = new Seat(_display);
            _registry.bind(name, iface, version, _seat);
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
    public boolean pointerEnter(int serial, int surface, int x, int y) { _cursor.attach(_pointer, serial); return true; }
    public boolean pointerLeave(int serial, int surface) { return true; }
    public boolean pointerMove(int time, int x, int y) { return true; }
    public boolean pointerButton(int serial, int time, int button, int state) { return true; }
}
