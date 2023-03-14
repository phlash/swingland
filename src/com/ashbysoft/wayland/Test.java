package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Test implements Display.Listener, Registry.Listener {
    public static void main(String[] args) {
        new Test().run();
    }
    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private Surface _surface;
    private Shm _shm;
    private ShmPool _shmpool;
    private int _poolsize;
    private Buffer _buffer;
    private int _bufsize;
    public void run() {
        _display = new Display();
        _display.addListener(this);
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        System.out.println("roundtripping..");
        _display.roundtrip();
        if (null == _compositor)
            throw new RuntimeException("oops: did not see a compositor!");
        if (null == _shm)
            throw new RuntimeException("oops: did not see an shm");
        _surface = new Surface(_display);
        _compositor.createSurface(_surface);
        System.out.println("roundtripping..");
        _display.roundtrip();
        _poolsize = 800 * 600 * 4;
        _shmpool = _shm.createPool(_poolsize);
        _bufsize = _poolsize;
        _buffer = _shmpool.createBuffer(0, 800, 600, 800*4, 0);
        ByteBuffer pixels = _buffer.get();
        pixels.rewind();
        for (int y=0; y<600; y++)
            for (int x=0; x<800; x++)
                pixels.putInt(0xff00ff00);
        _surface.attach(_buffer, 0, 0);
        _surface.damageBuffer(0, 0, 800, 600);
        _surface.commit();
        System.out.println("pumping..");
        while (_display.dispatch())
            try { Thread.currentThread().sleep(1000); } catch (Exception e) {}
    }
    public void error(int id, int code, String msg) {
        System.err.println("OOPS: object="+id+" code="+code+" message="+msg);
    }
    public boolean global(int name, String iface, int version) {
        if (iface.equals("wl_compositor")) {
            System.out.println("binding compositor..");
            _compositor = new Compositor(_display);
            _registry.bind(name, iface, version, _compositor);
        } else if (iface.equals("wl_shm")) {
            System.out.println("binding shm..");
            _shm = new Shm(_display);
            _registry.bind(name, iface, version, _shm);
        }
        return true;
    }
    public boolean remove(int name) {
        System.out.println("global remove: "+name);
        return true;
    }
}