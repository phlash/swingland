package com.ashbysoft.swingland;

import java.util.ArrayList;

public class GraphicsDevice {
    private String _make, _model, _name, _desc;
    private ArrayList<GraphicsConfiguration> _configs = new ArrayList<>();
    private ArrayList<DisplayMode> _modes = new ArrayList<>();
    private Window _fullscreen;

    protected GraphicsDevice(String make, String model, String name, String desc) { _make = make; _model = model; _name = name; _desc = desc; }
    // package-private config manipulation
    void addConfig(GraphicsConfiguration c) {
        _configs.add(c);
    }
    void remConfig(GraphicsConfiguration c) {
        _configs.remove(c);
    }
    void addMode(DisplayMode d) {
        _modes.add(d);
    }
    void remMode(DisplayMode d) {
        _modes.remove(d);
    }
    // API accessors
    public String getIDstring() {
        String rv = _make + '/' + _model;
        if (_name != null)
            rv = rv + '@' + _name;
        return rv;
    }
    public GraphicsConfiguration getDefaultConfiguration() {
        if (_configs.size() > 0)
            return _configs.get(0);
        return null;
    }
    public GraphicsConfiguration[] getConfigurations() {
        return (GraphicsConfiguration[])_configs.toArray();
    }
    public DisplayMode getDisplayMode() {
        if (_modes.size() > 0)
            return _modes.get(0);
        return null;
    }
    public DisplayMode[] getDisplayModes() {
        return (DisplayMode[])_modes.toArray();
    }
    public void setDisplayMode(DisplayMode d) {
        throw new IllegalArgumentException("not implemented, sorry :(");
    }
    public Window getFullScreenWindow() { return _fullscreen; }
    public void setFullScreenWindow(Window w) {
        if (w != null)
            w.setFullscreen(this);
        else if (_fullscreen != null)
            _fullscreen.unsetFullscreen(this);
        _fullscreen = w;
    }
}
