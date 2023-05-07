package com.ashbysoft.swingland;

import java.util.LinkedList;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.ActionListener;

public class Timer {
    // shared list of all timers, in absolute time execution order
    private static LinkedList<Timer> _timers = new LinkedList<>();

    private Logger _log = new Logger("[Timer@"+hashCode()+"]");
    private Object _lock;
    private LinkedList<ActionListener> _listeners = new LinkedList<>();
    // requested state
    private String _command;
    private int _delay;
    private boolean _doRepeats;
    private boolean _doRunning;
    // active state
    private long _nextRun;

    public Timer(int delay, ActionListener listener) {
        _log.error("<init>("+delay+","+listener.toString()+")");
        _listeners.add(listener);
        _delay = delay;
        _nextRun = -1L;
        _doRepeats = true;
        _doRunning = false;
        _lock = new Object();
    }
    public void addActionListener(ActionListener l) {
        synchronized (_lock) {
            _listeners.add(l);
        }
    }
    public void removeActionListener(ActionListener l) {
        synchronized (_lock) {
            _listeners.remove(l);
        }
    }
    public String getActionCommand() { return _command; }
    public void setActionCommand(String command) {
        synchronized (_lock) {
            _command = command;
        }
    }
    public int getDelay() { return _delay; }
    public void setDelay(int d) {
        synchronized (_lock) {
            _delay = d;
        }
    }
    public boolean isRepeats() { return _doRepeats; }
    public void setRepeats(boolean r) {
        synchronized (_lock) {
            _doRepeats = r;
        }
    }
    public boolean isRunning() { return _doRunning; }
    public void start() {
        _log.error("start()");
        synchronized (_lock) {
            _doRunning = true;
            // only schedule once..
            synchronized (_timers) {
                if (!_timers.contains(this))
                    reschedule(System.currentTimeMillis());
            }
        }
    }
    public void stop() {
        _log.error("stop()");
        synchronized (_lock) {
            _doRunning = false;
            synchronized (_timers) {
                _timers.remove(this);
            }
        }
    }
    public void restart() {
        _log.error("restart()");
        synchronized (_lock) {
            _doRunning = true;
            synchronized (_timers) {
                _timers.remove(this);
                reschedule(System.currentTimeMillis());
            }
        }
    }
    protected void fireActionPerformed() {
        _log.error("fireActionPerformed()");
        LinkedList<ActionListener> safe;
        ActionEvent e;
        synchronized (_lock) {
            safe = new LinkedList<ActionListener>(_listeners);
            e = new ActionEvent(this, ActionEvent.ACTION_FIRED, _command);
        }
        for (var l : safe)
            l.actionPerformed(e);
    }

    private void reschedule(long now) {
        // put ourselves back in the queue at the right point
        _log.error("reschedule("+now+")");
        _nextRun = now + _delay;
        synchronized (_timers) {
            for (int i = 0; i < _timers.size(); i += 1) {
                if (_nextRun < _timers.get(i)._nextRun) {
                    _timers.add(i, this);
                    return;
                }
            }
            _timers.addLast(this);
        }
    }

    // package-private method that UI thread calls every 10msec (ish)
    static void runTimers() {
        // anything timed out?
        Timer next;
        synchronized (_timers) {
            next = _timers.size() > 0 ? _timers.remove() : null;
        }
        if (next != null) {
            long now = System.currentTimeMillis();
            if (now >= next._nextRun) {
                // fire timer events
                next.fireActionPerformed();
                // if repeating, re-schedule
                if (next.isRepeats()) {
                    synchronized (_timers) {
                        next.reschedule(now);
                    }
                }
            } else {
                // not yet put back at fromt of queue
                _timers.addFirst(next);
            }
        }
    }
}
