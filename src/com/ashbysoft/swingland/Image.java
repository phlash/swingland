package com.ashbysoft.swingland;

import java.nio.ByteBuffer;

import com.ashbysoft.swingland.image.ImageObserver;
import com.ashbysoft.swingland.image.ImageProducer;

public abstract class Image {
    public Image() {}
    public void flush() {}
    public abstract Graphics getGraphics();
    public abstract int getWidth(ImageObserver obs);
    public abstract int getHeight(ImageObserver obs);
    public Image getScaledInstance(int w, int h, int hints) { return null; }
    public abstract ImageProducer getSource();
    // accessor to package-private constructor for Graphics objects
    protected Graphics getGraphics(ByteBuffer b, int w, int h) {
        return new Graphics(b, w, h, null, null);
    }
}
