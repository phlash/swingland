package com.ashbysoft.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Output _out = new Output();
    private String _pfx;
    public Logger(String pfx) { _pfx = pfx; }
    public String getPfx() { return _pfx; }
    public void setPfx(String pfx) { _pfx = pfx; }
    public void log(int level, String msg) {
        _out.write(level, _pfx, msg);
    }
    public void detail(String msg) {
        _out.write(2, _pfx, msg);
    }
    public void info(String msg) {
        _out.write(1, _pfx, msg);
    }
    public void error(String msg) {
        _out.write(0, _pfx, msg);
    }
    public void fatal(String msg) {
        _out.write(-1, _pfx, msg);
    }
    public int level() {
        return _out.level();
    }

    static class Output {
        private int _level = getLevel();
        private String[] _detail = getDetail();
        private int _pos = -1;
        private DateTimeFormatter _dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Output() {
            System.out.println("[LOG@"+level()+"/"+buffer()+"]");
        }
        int level() { return _level; }
        int buffer() { return _detail.length; }
        void write(int level, String pfx, String msg) {
            // format a log message
            String log = "("+_dtf.format(LocalDateTime.now())+"/"+Thread.currentThread().getName()+"/"+level+")"+pfx+msg;
            // always accumulate messages into the buffer..
            synchronized (_detail) {
                _pos = (_pos+1)%_detail.length;
                _detail[_pos] = log;
            }
            // dump if fatal
            if (level < 0)
                dump();
            // print if at or below log level
            else if (level <= _level)
                System.out.println(log);
        }
        void dump() {
            // dump the historical buffer in reverse chronological order
            System.out.println("! -- FATAL: trace follows in reverse chronological order --");
            synchronized (_detail) {
                int op = _pos;
                do {
                    System.out.println("! "+_detail[_pos]);
                    _pos -= 1;
                    if (_pos < 0) _pos = _detail.length-1;
                } while (_pos != op && _detail[_pos] != null);
            }
            System.out.println("! -- start of trace --");
        }
        private int getLevel() {
            // in priority order, we check: command line, environment, default
            String val = System.getProperty("ashbysoft.log.level");
            if (null==val)
                val = System.getenv("ASHBYSOFT_LOG_LEVEL");
            int lev = 0;
            try {
                lev = Integer.parseInt(val);
            } catch (NumberFormatException e) {}
            return lev;
        }
        private String[] getDetail() {
            String val = System.getProperty("ashbysoft.log.buffer");
            if (null==val)
                val = System.getenv("ASHBYSOFT_LOG_BUFFER");
            int len = 256;
            try {
                len = Integer.parseInt(val);
            } catch (NumberFormatException e) {}
            return new String[len];
        }
    }
}
