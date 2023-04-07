package com.ashbysoft.logger;

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
    public int level() {
        return _out.level();
    }

    static class Output {
        private int _level = getLevel();
        int level() { return _level; }
        void write(int level, String pfx, String msg) {
            if (level <= _level)
                System.out.println(pfx+msg);
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
            System.out.println("[LOG@"+lev+"]");
            return lev;
        }
    }
}
