package com.ashbysoft.swingland;

public class GraphicsConfiguration {
    private GraphicsDevice _device;
    private Rectangle _bounds;
    protected GraphicsConfiguration(GraphicsDevice device, Rectangle bounds) { _device = device; _bounds = bounds; }
    public GraphicsDevice getDevice() { return _device; }
    public Rectangle getBounds() { return _bounds; }
}
