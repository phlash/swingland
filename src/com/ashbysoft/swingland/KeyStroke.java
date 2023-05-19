package com.ashbysoft.swingland;

import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.event.KeyEvent;

public class KeyStroke {
    private final char _char;
    private final int _code;
    private final int _mods;
    private final boolean _onRel;
    private final Logger _log;
    private KeyStroke(Character ch, int code, int mods, boolean onRel) {
        _char = ch;
        _code = code;
        _mods = mods;
        _onRel = onRel;
        _log = new Logger("[KeyStroke:"+toString()+"]");
        _log.info("<init>()");
    }
    public char getKeyChar() { return _char; }
    public int getKeyCode() { return _code; }
    public int getModifiers() { return _mods; }
    public boolean isOnRelease() { return _onRel; }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if ((_mods & KeyEvent.SHIFT_DOWN_MASK) != 0) sb.append("Shift+");
        if ((_mods & KeyEvent.CTRL_DOWN_MASK) != 0) sb.append("Ctrl+");
        if ((_mods & KeyEvent.META_DOWN_MASK) != 0) sb.append("Super+");
        if ((_mods & KeyEvent.ALT_DOWN_MASK) != 0) sb.append("Alt+");
        if ((_mods & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0) sb.append("AltGr+");
        if (_code != KeyEvent.VK_UNDEFINED) {
            String s = DefaultKeymap.instance().getKeyName(_code);
            sb.append(s.length() > 0 ? s : "Unknown");
        } else {
            sb.append(_char);
        }
        return sb.toString();
    }
    boolean match(KeyEvent k) {
        boolean rv = false;
        if (_code != KeyEvent.VK_UNDEFINED) {
            // we're checking a code
            if (k.getKeyCode() == _code && k.getModifiersEx() == _mods && k.getID() == (_onRel ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED))
                rv = true;
        } else {
            // we're checking  a char
            if (k.getKeyChar() == _char && k.getModifiersEx() == _mods && k.getID() == KeyEvent.KEY_TYPED)
                rv = true;
        }
        if (rv) _log.info("match("+k.toString()+")="+rv);
        return rv;
    }
    public static KeyStroke getKeyStroke(char c) {
        return new KeyStroke(c, KeyEvent.VK_UNDEFINED, 0, true);
    }
    public static KeyStroke getKeyStroke(Character c, int mods) {
        return new KeyStroke(c, KeyEvent.VK_UNDEFINED, mods, true);
    }
    public static KeyStroke getKeyStroke(int code, int mods) {
        return new KeyStroke((char)KeyEvent.CHAR_UNDEFINED, code, mods, false);
    }
    public static KeyStroke getKeyStroke(int code, int mods, boolean onRel) {
        return new KeyStroke((char)KeyEvent.CHAR_UNDEFINED, code, mods, onRel);
    }
}
