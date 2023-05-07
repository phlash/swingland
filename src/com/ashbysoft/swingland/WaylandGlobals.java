package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.wayland.Display;
import com.ashbysoft.wayland.Registry;
import com.ashbysoft.wayland.Buffer;
import com.ashbysoft.wayland.Compositor;
import com.ashbysoft.wayland.XdgWmBase;
import com.ashbysoft.wayland.Shm;
import com.ashbysoft.wayland.Surface;
import com.ashbysoft.wayland.XdgToplevel;
import com.ashbysoft.wayland.Seat;
import com.ashbysoft.wayland.Keyboard;
import com.ashbysoft.wayland.Output;
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
    private LinkedList<Output> _outputs;
    private HashMap<GraphicsDevice, Integer> _gdMap;
    private Thread _uiThread;
    private LinkedList<Window> _repaints;
    private LinkedList<Runnable> _runnables;
    private LinkedList<Window> _popups;
    private HashMap<Integer, Window> _windows;
    private int _keyboardWindow;
    private int _pointerWindow;

    public WaylandGlobals() {
        _log.info("<init>()");
        _outputs = new LinkedList<>();
        _display = new Display();
        _registry = new Registry(_display);
        _registry.addListener(this);
        _display.getRegistry(_registry);
        // wait for all registry info
        _display.roundtrip();
        if (null == _compositor || null == _xdgWmBase || null == _shm) {
            String oops = "missing a required global object in Wayland: compositor="+_compositor+" xdgWmBase="+_xdgWmBase+" shm="+_shm;
            _log.error(oops);
            throw new RuntimeException(oops);
        }
        _xdgWmBase.addListener(this);
        _repaints = new LinkedList<>();
        _runnables = new LinkedList<>();
        _popups = new LinkedList<>();
        _windows = new HashMap<>();
        // wait for all bound objects callbacks
        _display.roundtrip();
        // initialise GraphicsEnvironment from known outputs
        _gdMap = new HashMap<>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (int i = 0; i < _outputs.size(); i += 1) {
            Output o = _outputs.get(i);
            if (!o.isDone()) {
                _log.error("output("+o.getID()+") is not done");
                continue;
            }
            GraphicsDevice gd = new GraphicsDevice();
            gd.addMode(new DisplayMode(o.getGeometryW(), o.getGeometryH(), 32, o.getRefreshRate()));
            gd.addConfig(new GraphicsConfiguration(gd, new Rectangle(o.getGeometryX(), o.getGeometryY(), o.getModeWidth(), o.getModeHeight())));
            ge.addDevice(gd);
            _gdMap.put(gd, i);
        }
        // create UI thread as non-daemon: holds the application active even if main exits
        _uiThread = new Thread(this);
        _uiThread.setDaemon(false);
        _uiThread.setName("SwinglandUI");
        _uiThread.start();
    }
    public Display display() { return _display; }
    public Compositor compositor() { return _compositor; }
    public XdgWmBase xdgWmBase() { return _xdgWmBase; }
    public Shm shm() { return _shm; }
    public Output[] outputs() { return (Output[])_outputs.toArray(); }
    public Output findOutput(GraphicsDevice gd) {
        if (_gdMap.containsKey(gd))
            return _outputs.get(_gdMap.get(gd));
        return null;
    }
    public GraphicsDevice findDevice(int outID) {
        for (int i = 0; i < _outputs.size(); i += 1) {
            Output o = _outputs.get(i);
            if (o.getID() == outID) {
                for (var e : _gdMap.entrySet()) {
                    if (e.getValue() == i)
                        return e.getKey();
                }
            }
        }
        return null;
    }
    public void register(int id, Window w) {
        _log.info("register("+id+","+w.getName()+")");
        synchronized(_windows) {
            _windows.put(id, w);
        }
    }
    public void unregister(int id) {
        _log.info("unregister("+id+")");
        Window w;
        synchronized(_windows) {
            w = _windows.remove(id);
        }
        // remove any pending repaints
        if (w != null)
            _repaints.remove(w);
    }
    private Window findWindow(int id) {
        synchronized(_windows) {
            return _windows.get(id);
        }
    }
    public void pushPopup(Window w) {
        synchronized(_popups) {
            _popups.add(w);
        }
    }
    public void removePopup(Window w) {
        synchronized(_popups) {
            _popups.remove(w);
        }
    }
    public Window topPopup() {
        synchronized(_popups) {
            if (_popups.size() > 0)
                return _popups.peekLast();
        }
        return null;
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
        } else if (iface.equals("wl_output")) {
            Output o = new Output(_display);
            _outputs.add(o);
            _registry.bind(name, iface, version, o);
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
    private int _repeatDelay = -1;
    private int _repeatRate = -1;
    private int _repeatCode = -1;
    private int _repeatState = 0;
    private long _repeatStamp = -1L;
    public boolean keymap(int format, int fd, int size) {
        // TODO: use a real keymap once we can read the fd..
        _keymap = DefaultKeymap.instance();
        return true;
    }
    public boolean keyboardEnter(int serial, int surface, int[] keys) {
        _keyboardWindow = surface;
        for (int k : keys)
            key(serial, 0, k, KeyEvent.KEY_PRESSED);
        return true;
    }
    public boolean keyboardLeave(int serial, int surface) {
        _repeatState = 0;
        return true;
    }
    public boolean key(int serial, int time, int keyCode, int state) {
        _repeatStamp = System.currentTimeMillis();
        _repeatCode = keyCode;
        if (Keyboard.KEY_PRESSED == state)
            _repeatState = 1;
        else
            _repeatState = 0;
        return keySend(serial, time, keyCode, state);
    }
    private boolean keySend(int serial, int time, int keyCode, int state) {
        // Keyboard input always goes to any popup present, before last entered window
        Window w = topPopup();
        if (null == w) w = findWindow(_keyboardWindow);
        if (w != null) {
            KeyEvent e = new KeyEvent(this, state, _keymap.getModifiersEx(), keyCode, (char)KeyEvent.CHAR_UNDEFINED);
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
    public boolean repeat(int rate, int delay) { _repeatRate = rate; _repeatDelay = delay; return true; }
    // Pointer.Listener
    private int _pointerX;
    private int _pointerY;
    private int _bdragged;
    private int _lastEnter;
    private int _lastSerial;
    private Surface _cursorSurface;
    private Surface cursorSurface() {
        if (null == _cursorSurface) {
            _cursorSurface = new Surface(_display);
            _compositor.createSurface(_cursorSurface);
        }
        return _cursorSurface;
    }
    private boolean pointerSend(MouseEvent m) {
        Window w = findWindow(_pointerWindow);
        if (w != null) {
            w.dispatchEvent(m);
        }
        return true;
    }
    public boolean pointerEnter(int serial, int surface, int x, int y) {
        _pointerWindow = surface;
        // save enter serial number so we can update cursor hotspot later
        _lastEnter = _lastSerial = serial;
        _pointer.setCursor(serial, cursorSurface(), 0, 0);
        // dispatching the first pointer event will generate a MOUSE_ENTERED, which in turn will render the cursor on it's surface.
        return pointerMove(0, x, y);
    }
    public boolean pointerLeave(int serial, int surface) {
        _lastSerial = serial;
        _bdragged = 0;
        return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_EXITED, _keymap.getModifiersEx(), _pointerX >> 8, _pointerY >> 8, -1, -1));
    }
    public boolean pointerMove(int time, int x, int y) {
        _pointerX = x;
        _pointerY = y;
        return pointerSend(new MouseEvent(this, _bdragged != 0 ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVE,
            _keymap.getModifiersEx(), _pointerX >> 8, _pointerY >> 8, -_bdragged != 0 ? _bdragged : -1, _bdragged != 0 ? MouseEvent.BUTTON_PRESSED : -1));
    }
    public boolean pointerButton(int serial, int time, int button, int state) {
        _lastSerial = serial;
        // map button codes & state
        int mbutton = Pointer.BUTTON_LEFT == button ? MouseEvent.BUTTON1 : Pointer.BUTTON_RIGHT == button ? MouseEvent.BUTTON2 : MouseEvent.BUTTON3;
        int mstate = Pointer.BUTTON_RELEASED == state ? MouseEvent.BUTTON_RELEASED : MouseEvent.BUTTON_PRESSED;
        // update drag button
        _bdragged = MouseEvent.BUTTON_PRESSED == mstate ? mbutton : 0;
        return pointerSend(new MouseEvent(this, MouseEvent.MOUSE_BUTTON, _keymap.getModifiersEx(), _pointerX >> 8, _pointerY >> 8, mbutton, mstate));
    }
    public boolean pointerFrame() { return true; }

    // cursor update
    public void updateCursor(Buffer b, Dimension d, Point h) {
        Surface s = cursorSurface();
        _pointer.setCursor(_lastEnter, s, h._x, h._y);
        s.attach(b, 0, 0);
        s.damageBuffer(0, 0, d._w, d._h);
        s.commit();
    }

    // interactive reposition / resize requests
    public void reposition(XdgToplevel t) {
        _log.info("reposition()");
        t.move(_seat, _lastSerial);
    }
    public void resize(XdgToplevel t, int edges) {
        _log.info("resize("+edges+")");
        t.resize(_seat, _lastSerial, edges);
    }

    // repaint request queue
    public void repaint(Window w) {
        boolean add = false;
        synchronized(_repaints) {
            // coalesce repaints for the same window
            if (!_repaints.contains(w)) {
                _repaints.add(w);
                add = true;
            }
        }
        _log.info("repaint("+w.getName()+")?"+add);
    }
    // general runnable queue
    public void queue(Runnable r) {
        _log.info("queue("+r.toString()+")");
        synchronized(_runnables) {
            _runnables.add(r);
        }
    }

    // UI thread
    public void run() 
    {
        _log.info("--> UI thread");
        // we run until:
        // - we have seen at least one window
        // - there are now zero registered windows
        boolean canExit = false;
        while (true) {
            // priority order of work..
            // all pending Wayland events
            int n = _display.dispatch();
            if (n < 0)
                break;
            else if (n > 0)
                _log.info("<-- events:n="+n);
            // one repaint if one is waiting
            if (_repaints.size() > 0) {
                Window w;
                synchronized(_repaints) {
                    w = _repaints.remove();
                }
                if (w != null) {
                    _log.info("--> render");
                    w.render();
                    _log.info("<-- render");
                }
            // OR one runnable job if one is waiting
            } else if (_runnables.size() > 0) {
                Runnable r;
                synchronized(_runnables) {
                    r = _runnables.remove();
                }
                if (r != null) {
                    _log.info("--> queued");
                    r.run();
                    _log.info("<-- queued");
                }
            }
            // key repeat processing
            if (_repeatState > 0 && _repeatDelay > 0 && _repeatRate > 0) {
                long now = System.currentTimeMillis();
                boolean send = false;
                if (1 == _repeatState) {            // delaying..
                    if (now-_repeatStamp >= _repeatDelay) {
                        _repeatState = 2;
                        _repeatStamp = now;
                        send = true;
                    }
                } else if (2 == _repeatState) {     // repeating..
                    int period = 1000 / _repeatRate;
                    if (now-_repeatStamp >= period) {
                        _repeatStamp = now;
                        send = true;
                    }
                }
                if (send) {
                    _log.info("--> key repeat("+_repeatCode+")");
                    keySend(0, 0, _repeatCode, Keyboard.KEY_RELEASED);
                    keySend(0, 0, _repeatCode, Keyboard.KEY_PRESSED);
                    _log.info("<-- key repeat("+_repeatCode+")");
                }
            }
            // Timer processing
            Timer.runTimers();
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
        _display.roundtrip();
        _display.close();
        _log.info("<-- UI thread");
    }
}
