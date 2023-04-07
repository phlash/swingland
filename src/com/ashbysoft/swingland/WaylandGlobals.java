package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.wayland.Display;
import com.ashbysoft.wayland.Registry;
import com.ashbysoft.wayland.Compositor;
import com.ashbysoft.wayland.XdgWmBase;
import com.ashbysoft.wayland.Shm;
import com.ashbysoft.wayland.Surface;
import com.ashbysoft.wayland.Seat;
import com.ashbysoft.wayland.Keyboard;
import com.ashbysoft.wayland.Pointer;
import com.ashbysoft.swingland.event.*;

import java.util.LinkedList;
import java.util.HashMap;

// package-private singleton container for all Wayland global objects and UI thread holder
class WaylandGlobals implements
    Runnable,
    Registry.Listener,
    XdgWmBase.Listener,
    Seat.Listener,
    Keyboard.Listener,
    Pointer.Listener {

    // singleton factory
    private static WaylandGlobals _instance;
    private static Object _lock = new Object();
    static WaylandGlobals instance() {
        synchronized(_lock) {
            if (null == _instance)
                _instance = new WaylandGlobals();
        }
        return _instance;
    }

    private Logger _log = new Logger("[WaylandGlobals]:");
    private Display _display;
    private Registry _registry;
    private Compositor _compositor;
    private XdgWmBase _xdgWmBase;
    private Shm _shm;
    private Seat _seat;
    private Keyboard _keyboard;
    private Pointer _pointer;
    private Thread _uiThread;
    private LinkedList<Window> _repaints;
    private LinkedList<Runnable> _runnables;
    private HashMap<Integer, Window> _windows;
    private int _keyboardWindow;
    private int _pointerWindow;

    public WaylandGlobals() {
        _log.info("<init>()");
        _display = new Display();
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        _display.roundtrip();
        if (null == _compositor || null == _xdgWmBase || null == _shm) {
            String oops = "missing a required global object in Wayland: compositor="+_compositor+" xdgWmBase="+_xdgWmBase+" shm="+_shm;
            _log.error(oops);
            throw new RuntimeException(oops);
        }
        _xdgWmBase.addListener(this);
        _repaints = new LinkedList<>();
        _runnables = new LinkedList<>();
        _windows = new HashMap<Integer, Window>();
        _uiThread = new Thread(this);
        // We hold the application active even if main exits
        _uiThread.setDaemon(false);
        _uiThread.setName("SwinglandUI");
        _uiThread.start();
    }
    public Display display() { return _display; }
    public Compositor compositor() { return _compositor; }
    public XdgWmBase xdgWmBase() { return _xdgWmBase; }
    public Shm shm() { return _shm; }
    public void register(int id, Window w) {
        _log.info("register("+id+","+w.getName()+")");
        synchronized(_windows) {
            _windows.put(id, w);
        }
    }
    public void unregister(int id) {
        _log.info("unregister("+id+")");
        synchronized(_windows) {
            _windows.remove(id);
        }
    }
    private Window findWindow(int id) {
        synchronized(_windows) {
            return _windows.get(id);
        }
    }
    // Registry.Listener
    public boolean global(int name, String iface, int version) {
        if (iface.equals("wl_compositor")) {
            _compositor = new Compositor(_display);
            _registry.bind(name, iface, version, _compositor);
        } else if (iface.equals("wl_shm")) {
            _shm = new Shm(_display);
            _registry.bind(name, iface, version, _shm);
        } else if (iface.equals("xdg_wm_base")) {
            _xdgWmBase = new XdgWmBase(_display);
            _registry.bind(name, iface, version, _xdgWmBase);
        } else if (iface.equals("wl_seat")) {
            _seat = new Seat(_display);
            _seat.addListener(this);
            _registry.bind(name, iface, version, _seat);
        }
        return true;
    }
    public boolean remove(int name) { return true; }
    // XdgWmBase.Listener
    public boolean ping(int serial) {
        return _xdgWmBase.pong(serial);
    }
    // Seat.Listener
    public boolean seatCapabilities(int caps) {
        if ((caps & Seat.Listener.KEYBOARD) != 0) {
            _keyboard = new Keyboard(_display);
            _keyboard.addListener(this);
            _seat.getKeyboard(_keyboard);
        }
        if ((caps & Seat.Listener.POINTER) != 0) {
            _pointer = new Pointer(_display);
            _pointer.addListener(this);
            _seat.getPointer(_pointer);
        }
        return true;
    }
    public boolean seatName(String name) { return true; }
    // Keyboard.Listener
    private Keymap _keymap;
    public boolean keymap(int format, int fd, int size) {
        // XXX:TODO: use a real keymap once we can read the fd..
        _keymap = new DefaultKeymap();
        return true;
    }
    public boolean keyboardEnter(int serial, int surface, int[] keys) {
        _keyboardWindow = surface;
        for (int k : keys)
            key(serial, 0, k, KeyEvent.KEY_PRESSED);
        return true;
    }
    public boolean keyboardLeave(int serial, int surface) { return true; }
    public boolean key(int serial, int time, int keyCode, int state) {
        Window w = findWindow(_keyboardWindow);
        if (w != null) {
            KeyEvent e = new KeyEvent(this, state, keyCode, '-');
            w.dispatchEvent(e);
            if (KeyEvent.KEY_PRESSED == state) {
                e = _keymap.mapCode(keyCode);
                if (e != null)
                    w.dispatchEvent(e);
            }
        }
        return true;
    }
    public boolean modifiers(int serial, int depressed, int latched, int locked, int group) {
        _keymap.modifiers(depressed, latched, locked, group);
        return true;
    }
    public boolean repeat(int rate, int delay) { return true; } // XXX:TODO: key repeats
    // Pointer.Listener
    private int _pointerX;
    private int _pointerY;
    private boolean pointerSend(MouseEvent m) {
        Window w = findWindow(_pointerWindow);
        if (w != null) {
            w.dispatchEvent(m);
        }
        return true;
    }
    public boolean pointerEnter(int serial, int surface, int x, int y) {
        _pointerWindow = surface;
        // we associate the cursor surface with the window surface here (needs serial)
        Window w = findWindow(_pointerWindow);
        if (w != null) {
            Surface s = w.getCursorSurface();
            _pointer.setCursor(serial, s, 0, 0);    // XXX:TODO: hotspot info
        }
        // dispatching the first pointer event will generate a MOUSE_ENTERED, which in turn will render the cursor on it's surface.
        return pointerMove(0, x, y);
    }
    public boolean pointerLeave(int serial, int surface) {
        return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_EXITED, _pointerX >> 8, _pointerY >> 8, -1, -1));
    }
    public boolean pointerMove(int time, int x, int y) {
        _pointerX = x;
        _pointerY = y;
        return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_MOVE, _pointerX >> 8, _pointerY >> 8, -1, -1));
    }
    public boolean pointerButton(int serial, int time, int button, int state) {
        return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_BUTTON, _pointerX >> 8, _pointerY >> 8, button, state));
    }

    // repaint request queue
    public void repaint(Window w) {
        synchronized(_repaints) {
            // coalesce repaints for the same window
            if (!_repaints.contains(w))
                _repaints.add(w);
        }
    }
    // general runnable queue
    public void queue(Runnable r) {
        synchronized(_runnables) {
            _runnables.add(r);
        }
    }

    // UI thread
    public void run() 
    {
        // we run until:
        // - we have seen at least one window
        // - there are now zero registered windows
        boolean canExit = false;
        while (true) {
            // priority order of work..
            // all pending Wayland events
            _display.dispatch();
            // one repaint if one is waiting
            if (_repaints.size() > 0) {
                Window w;
                synchronized(_repaints) {
                    w = _repaints.remove();
                }
                if (w != null)
                    w.render();
            // OR one runnable job if one is waiting
            } else if (_runnables.size() > 0) {
                Runnable r;
                synchronized(_runnables) {
                    r = _runnables.remove();
                }
                if (r != null)
                    r.run();
            }
            // termination check
            int count;
            synchronized(_windows) {
                count = _windows.size();
            }
            if (count > 0)
                canExit = true;
            else if (canExit)
                break;
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        _display.close();
    }
}
