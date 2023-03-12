package com.ashbysoft.wayland;

import java.nio.ByteBuffer;

public class Test implements Registry.Listener {
    public static void main(String[] args) {
        new Test().run();
    }
    private Display _display;
    private Registry _registry;
    private WaylandObject _compositor;
    public void run() {
        _display = new Display();
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        System.out.println("roundtripping..");
        _display.roundtrip();
        System.out.println("pumping..");
        while (_display.dispatch())
            try { Thread.currentThread().sleep(1000); } catch (Exception e) {}
    }
    public boolean global(int name, String iface, int version) {
        System.out.println("global: "+name+"="+iface);
        if (iface.equals("wl_compositor")) {
            System.out.println("binding..");
            _compositor = new WaylandObject() {
                public boolean handle(int oid, int op, int size, ByteBuffer b) { return true; }
            };
            _registry.bind(name, _compositor);
        }

        return true;
    }
    public boolean remove(int name) {
        System.out.println("global remove: "+name);
        return true;
    }
}