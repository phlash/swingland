package com.ashbysoft.swingland;

import com.ashbysoft.wayland.Keyboard;
import com.ashbysoft.swingland.event.KeyEvent;
import java.util.List;

public class DefaultKeymap implements Keymap {
    private List<Integer> _AtoZ = List.of(
        KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F,
        KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L,
        KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R,
        KeyEvent.VK_S, KeyEvent.VK_T, KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X,
        KeyEvent.VK_Y, KeyEvent.VK_Z
    );
    private List<Integer> _Nums = List.of(
        KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
        KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
    );
    private List<Character> _NumsShift = List.of(
        ')', '!', '"', '£', '$', '%', '^', '&', '*', '('
    );
    private List<Integer> _Punc = List.of(
        KeyEvent.VK_GRAVE, KeyEvent.VK_TAB, KeyEvent.VK_102ND, KeyEvent.VK_MINUS, KeyEvent.VK_EQUAL,
        KeyEvent.VK_LEFTBRACE, KeyEvent.VK_RIGHTBRACE, KeyEvent.VK_SEMICOLON, KeyEvent.VK_APOSTROPHE,
        KeyEvent.VK_BACKSLASH, KeyEvent.VK_ENTER, KeyEvent.VK_COMMA, KeyEvent.VK_DOT, KeyEvent.VK_SLASH,
        KeyEvent.VK_BACKSPACE, KeyEvent.VK_KPSLASH, KeyEvent.VK_KPASTERISK, KeyEvent.VK_KPMINUS,
        KeyEvent.VK_KPPLUS, KeyEvent.VK_KPENTER, KeyEvent.VK_SPACE
    );
    private List<Character> _PuncTo = List.of(
        '`', '\t', '\\', '-', '=', '[', ']', ';', '\'', '#', '\r', ',', '.', '/', '\b', '/', '*', '-', '+', '\r', ' '
    );
    private List<Character> _PuncShift = List.of(
        '¬', '\t', '|', '_', '+', '{', '}', ':', '@', '~', '\r', '<', '>', '?', '\b', '/', '*', '-', '+', '\r', ' '
    );
    private List<Integer> _KPNums = List.of(
        KeyEvent.VK_KP0, KeyEvent.VK_KP1, KeyEvent.VK_KP2, KeyEvent.VK_KP3, KeyEvent.VK_KP4,
        KeyEvent.VK_KP5, KeyEvent.VK_KP6, KeyEvent.VK_KP7, KeyEvent.VK_KP8, KeyEvent.VK_KP9
    );
    private int _keymods;
    public void modifiers(int depressed, int latched, int locked, int group) {
        _keymods = depressed | latched | locked;
    }
    public KeyEvent mapCode(int keyCode) {
        // A-Z can be affected by MOD_CTRL, MOD_SHIFT and MOD_CAPSLOCK
        // all printables can be affected by MOD_SHIFT
        // keypad 0-9 can be affected by MOD_NUMLOCK
        // any other modifiers do NOT get mapped to typed events

        // holding Alt, AltGr or Super prevents typed events
        if ((_keymods & (Keyboard.MOD_ALT | Keyboard.MOD_ALTGR | Keyboard.MOD_SUPER)) != 0)
            return null;
        // holding Ctrl maps A-Z to low valued ASCII control chars (0-25)
        if ((_keymods & Keyboard.MOD_CTRL) != 0) {
            int pos = _AtoZ.indexOf(keyCode);
            if (pos >= 0) {
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, (char)pos);
            }
            return null;
        }
        // CapsLock OR Shift (but not both) maps A-Z to uppercase
        boolean cl = (_keymods & Keyboard.MOD_CAPSLOCK) != 0;
        boolean sh = (_keymods & Keyboard.MOD_SHIFT) != 0;
        if ((cl && !sh) || (sh && !cl)) {
            int pos = _AtoZ.indexOf(keyCode);
            if (pos >= 0) {
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, (char)('A'+pos));
            }
        }
        // no CapsLock or Shift (or both) maps A-Z to lowercase
        if ((cl && sh) || (!cl && !sh)) {
            int pos = _AtoZ.indexOf(keyCode);
            if (pos >= 0) {
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, (char)('a'+pos));
            }
        }
        // Numeric keys map when shifted or not
        {
            int pos = _Nums.indexOf(keyCode);
            if (pos >= 0)
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, sh ? _NumsShift.get(pos) : (char)('0'+pos));
        }
        // various punctuation keys map when shifted or not
        {
            int pos = _Punc.indexOf(keyCode);
            if (pos >= 0)
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, sh ? _PuncShift.get(pos) : _PuncTo.get(pos));
        }
        // NumLock maps keypad to numbers
        if ((_keymods & Keyboard.MOD_NUMLOCK) != 0) {
            int pos = _KPNums.indexOf(keyCode);
            if (pos >= 0) {
                return new KeyEvent(this, KeyEvent.KEY_TYPED, keyCode, (char)('0'+pos));
            }
        }
        // or nowt.
        return null;
    }
}