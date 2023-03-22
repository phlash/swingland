// XXX:TODO

package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.EventListener;
import java.util.LinkedList;

public class JComponent extends Container {
    protected final LinkedList<EventListener> _listeners = new LinkedList<EventListener>();
    public void setDoubleBuffered(boolean db) {}
    protected void addEventListener(EventListener l) { _listeners.add(l); }
    protected void removeEventListener(EventListener l) { _listeners.remove(l); }
}
