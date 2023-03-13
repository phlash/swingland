package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Test implements Registry.Listener {
    public static void main(String[] args) {
        new Test().run();
    }
    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private Surface _surface;
    public void run() {
        _display = new Display();
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        System.out.println("roundtripping..");
        _display.roundtrip();
        if (null == _compositor)
            throw new RuntimeException("oops: did not see a compositor!");
        //_surface = new Surface();
        //_compositor.createSurface(_surface);
        System.out.println("pumping..");
        while (_display.dispatch())
            try { Thread.currentThread().sleep(1000); } catch (Exception e) {}
    }
    public boolean global(int name, String iface, int version) {
        System.out.println("global: "+name+"="+iface);
        if (iface.equals("wl_compositor")) {
            System.out.println("binding..");
            _compositor = new Compositor(_display);
            _registry.bind(name, iface, version, _compositor);
        }

        return true;
    }
    public boolean remove(int name) {
        System.out.println("global remove: "+name);
        return true;
    }
}