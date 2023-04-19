package com.ashbysoft.swingland;

import java.util.ArrayList;

public class GraphicsEnvironment {
    // there can be only one.
    private GraphicsEnvironment() {}
    private static GraphicsEnvironment _instance;
    public static GraphicsEnvironment getLocalGraphicsEnvironment() {
        if (null == _instance)
            _instance = new GraphicsEnvironment();
        return _instance;
    }

    // package-private means to manipulate devices
    private ArrayList<GraphicsDevice> _devices = new ArrayList<>();
    void addDevice(GraphicsDevice d) {
        _devices.add(d);
    }
    void remDevice(GraphicsDevice d) {
        _devices.remove(d);
    }
    public GraphicsDevice getDefaultScreenDevice() {
        if (_devices.size() > 0)
            return _devices.get(0);
        return null;
    }
    public GraphicsDevice[] getScreenDevices() {
        return (GraphicsDevice[])_devices.toArray();
    }
}
