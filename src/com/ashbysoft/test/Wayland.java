package com.ashbysoft.test;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.wayland.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Wayland implements Display.Listener, Registry.Listener, XdgWmBase.Listener, Pointer.Listener {

    private Logger _log = new Logger("[Wayland]:");
    private class Cursor {
        // fixed cursor, 16x16 left arrow, hotspot at 0,0
        private static final short[] _cursor1 = {
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
        private static final short[] _cursor2 = {
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b1111111111111111,
            (short)0b1111111111111111,
            (short)0b1000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
            (short)0b0000000110000000,
        };
        private ShmPool _cursorShmpool;
        private Buffer _cursorBuffer;
        public Cursor(int which) {
            // create a cursor and attach to our pointer..
            short[] bits = which>0 ? _cursor2 : _cursor1;
            _cursorShmpool = _shm.createPool(bits.length*16*4);
            _cursorBuffer = _cursorShmpool.createBuffer(0, 16, 16, 16*4, 0);
            ByteBuffer pixels = _cursorBuffer.get();
            pixels.rewind();
            for (int y=0; y < bits.length; y += 1) {
                for (int x=0; x < 16; x+= 1) {
                    short m = (short)(1 << (15-x));
                    if ((bits[y] & m) > 0)
                        pixels.putInt(0xffffffff);
                    else
                        pixels.putInt(0);
                }
            }
        }
        public void attach(Surface surface) {
            surface.attach(_cursorBuffer, 0, 0);
            surface.damageBuffer(0, 0, 16, 16);
            surface.commit();
        }
        public void destroy() {
            if (_cursorBuffer != null) {
                _cursorBuffer.destroy();
                _cursorBuffer = null;
            }
            if (_cursorShmpool != null) {
                _cursorShmpool.destroy();
                _cursorShmpool = null;
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

        protected XdgCommon(int w, int h, int which) {
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
                    pixels.putInt(getPixel(x, y));
            _surface.attach(_buffer, 0, 0);
            _surface.damageBuffer(0, 0, _width, _height);
            _surface.commit();
        }
        protected int getPixel(int x, int y) { return 0xff000000; }
    }
    private class Toplevel extends XdgCommon implements XdgToplevel.Listener {
        private Random _rand;
        private XdgToplevel _xdgToplevel;
        protected Surface _cursorSurface;
        private Cursor _cursorOut;
        private Cursor _cursorIn;
        private final int _boxWidth  = 100;
        private final int _boxHeight = 100;
        public Toplevel() {
            super(640, 480, 0);
            _xdgToplevel = new XdgToplevel(_display);
            _xdgToplevel.addListener(this);
            _xdgSurface.getTopLevel(_xdgToplevel);
            _xdgToplevel.setTitle("Java wayland API!");
            _xdgToplevel.setAppID("com.ashbysoft.test.Wayland");
            _rand = new Random();
            _surface.commit();
            _display.roundtrip();
            _cursorOut = new Cursor(0);
            _cursorIn = new Cursor(1);
            done(0);
        }
        public boolean xdgToplevelConfigure(int w, int h, int[] states) { if (w>0 && h>0) { _width = w; _height = h; } return true; }
        public boolean xdgToplevelClose() { return true; }
        public int destroy() {
            if (_buffer != null) _buffer.destroy();
            if (_shmpool != null) _shmpool.destroy();
            _cursorIn.destroy();
            _cursorOut.destroy();
            _xdgToplevel.destroy();
            _xdgSurface.destroy();
            _surface.destroy();
            return _frames;
        }
        protected int getPixel(int x, int y) {
            // box boundary where cursor changes..
            int bxl = (_width-_boxWidth)/2;
            int bxr = (_width+_boxWidth)/2;
            int bxt = (_height-_boxHeight)/2;
            int bxb = (_height+_boxHeight)/2;
            if (y == bxt || y == bxb) {
                if (x >= bxl && x <= bxr)
                    return 0xffffffff;
            } else if (y > bxt && y < bxb) {
                if (x == bxl || x == bxr)
                    return 0xffffffff;
                else if (x > bxl && x < bxr)
                    return 0xff000000;
            }
            return (_rand.nextInt() & 0x00ffffff) | 0xff000000;
        }
        protected Surface getCursorSurface() {
            if (null == _cursorSurface) {
                _cursorSurface = new Surface(_display);
                _compositor.createSurface(_cursorSurface);
            }
            return _cursorSurface;
        }
        protected Cursor getCursor(int x, int y) {
            // box boundary where cursor changes..
            int bxl = (_width-_boxWidth)/2;
            int bxr = (_width+_boxWidth)/2;
            int bxt = (_height-_boxHeight)/2;
            int bxb = (_height+_boxHeight)/2;
            if (y >= bxt && y <= bxb && x >= bxl && x <= bxr)
                return _cursorIn;
            return _cursorOut;
        }
    }

    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private XdgWmBase _xdgWmBase;
    private Shm _shm;
    private Seat _seat;
    private Pointer _pointer;
    private Toplevel _toplevel;
    private Cursor _lastCursor;
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
        _toplevel = new Toplevel();
        _display.roundtrip();
        if (_seat != null) {
            _pointer = new Pointer(_display);
            _pointer.addListener(this);
            _seat.getPointer(_pointer);
        }
        _display.roundtrip();
        long start = System.currentTimeMillis();
        long now = 0;
        while (_display.dispatch()) {
            try { Thread.sleep(10); } catch (Exception e) {}
            now = System.currentTimeMillis();
            if (now > start+timeout)
                break;
        }
        int fps = (_toplevel.destroy()*1000)/(int)(now-start);
        if (_pointer != null) _pointer.release();
        _display.close();
        _log.error("---- Wayland test done (fps:"+fps+") ----");
    }
    public void error(int id, int code, String msg) {
        _log.error("OOPS: object="+id+" code="+code+" message="+msg);
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
    public boolean pointerEnter(int serial, int surface, int x, int y) {
        _log.error("pointerEnter()");
        _pointer.setCursor(serial, _toplevel.getCursorSurface(), 0, 0);
        return pointerMove(0, x, y);
    }
    public boolean pointerLeave(int serial, int surface) { return true; }
    public boolean pointerMove(int time, int x, int y) {
        x >>= 8;
        y >>= 8;
        Cursor c = _toplevel.getCursor(x, y);
        if (c != _lastCursor) {
            _log.error("pointerMove("+x+","+y+"):new cursor");
            _lastCursor = c;
            c.attach(_toplevel.getCursorSurface());
        }
        return true;
    }
    public boolean pointerButton(int serial, int time, int button, int state) { return true; }
}
