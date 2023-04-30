package com.ashbysoft.swingland.event;

import com.ashbysoft.swingland.DefaultKeymap;

public class KeyEvent extends AbstractEvent {
    public static final int VK_UNDEFINED = 0;
    public static final int VK_ESC = 1;
    public static final int VK_1 = 2;
    public static final int VK_2 = 3;
    public static final int VK_3 = 4;
    public static final int VK_4 = 5;
    public static final int VK_5 = 6;
    public static final int VK_6 = 7;
    public static final int VK_7 = 8;
    public static final int VK_8 = 9;
    public static final int VK_9 = 10;
    public static final int VK_0 = 11;
    public static final int VK_MINUS = 12;
    public static final int VK_EQUAL = 13;
    public static final int VK_BACKSPACE = 14;
    public static final int VK_TAB = 15;
    public static final int VK_Q = 16;
    public static final int VK_W = 17;
    public static final int VK_E = 18;
    public static final int VK_R = 19;
    public static final int VK_T = 20;
    public static final int VK_Y = 21;
    public static final int VK_U = 22;
    public static final int VK_I = 23;
    public static final int VK_O = 24;
    public static final int VK_P = 25;
    public static final int VK_LEFTBRACE = 26;
    public static final int VK_RIGHTBRACE = 27;
    public static final int VK_ENTER = 28;
    public static final int VK_LEFTCTRL = 29;
    public static final int VK_A = 30;
    public static final int VK_S = 31;
    public static final int VK_D = 32;
    public static final int VK_F = 33;
    public static final int VK_G = 34;
    public static final int VK_H = 35;
    public static final int VK_J = 36;
    public static final int VK_K = 37;
    public static final int VK_L = 38;
    public static final int VK_SEMICOLON = 39;
    public static final int VK_APOSTROPHE = 40;
    public static final int VK_GRAVE = 41;
    public static final int VK_LEFTSHIFT = 42;
    public static final int VK_BACKSLASH = 43;
    public static final int VK_Z = 44;
    public static final int VK_X = 45;
    public static final int VK_C = 46;
    public static final int VK_V = 47;
    public static final int VK_B = 48;
    public static final int VK_N = 49;
    public static final int VK_M = 50;
    public static final int VK_COMMA = 51;
    public static final int VK_DOT = 52;
    public static final int VK_SLASH = 53;
    public static final int VK_RIGHTSHIFT = 54;
    public static final int VK_KPASTERISK = 55;
    public static final int VK_LEFTALT = 56;
    public static final int VK_SPACE = 57;
    public static final int VK_CAPSLOCK = 58;
    public static final int VK_F1 = 59;
    public static final int VK_F2 = 60;
    public static final int VK_F3 = 61;
    public static final int VK_F4 = 62;
    public static final int VK_F5 = 63;
    public static final int VK_F6 = 64;
    public static final int VK_F7 = 65;
    public static final int VK_F8 = 66;
    public static final int VK_F9 = 67;
    public static final int VK_F10 = 68;
    public static final int VK_NUMLOCK = 69;
    public static final int VK_SCROLLLOCK = 70;
    public static final int VK_KP7 = 71;
    public static final int VK_KP8 = 72;
    public static final int VK_KP9 = 73;
    public static final int VK_KPMINUS = 74;
    public static final int VK_KP4 = 75;
    public static final int VK_KP5 = 76;
    public static final int VK_KP6 = 77;
    public static final int VK_KPPLUS = 78;
    public static final int VK_KP1 = 79;
    public static final int VK_KP2 = 80;
    public static final int VK_KP3 = 81;
    public static final int VK_KP0 = 82;
    public static final int VK_KPDOT = 83;
    public static final int VK_102ND = 86;
    public static final int VK_F11 = 87;
    public static final int VK_F12 = 88;
    public static final int VK_KPENTER = 96;
    public static final int VK_RIGHTCTRL = 97;
    public static final int VK_KPSLASH = 98;
    public static final int VK_SYSRQ = 99;
    public static final int VK_RIGHTALT = 100;
    public static final int VK_LINEFEED = 101;
    public static final int VK_HOME = 102;
    public static final int VK_UP = 103;
    public static final int VK_PAGEUP = 104;
    public static final int VK_LEFT = 105;
    public static final int VK_RIGHT = 106;
    public static final int VK_END = 107;
    public static final int VK_DOWN = 108;
    public static final int VK_PAGEDOWN = 109;
    public static final int VK_INSERT = 110;
    public static final int VK_DELETE = 111;
    public static final int VK_MACRO = 112;
    public static final int VK_MUTE = 113;
    public static final int VK_VOLUMEDOWN = 114;
    public static final int VK_VOLUMEUP = 115;
    public static final int VK_POWER = 116;
    public static final int VK_KPEQUAL = 117;
    public static final int VK_KPPLUSMINUS = 118;
    public static final int VK_PAUSE = 119;
    public static final int VK_SCALE = 120;
    public static final int CHAR_UNDEFINED = 65535;

    // event IDs
    public static final int KEY_RELEASED = 0;
    public static final int KEY_PRESSED = 1;
    public static final int KEY_TYPED = 2;

    private int _code;
    private char _char;
    public KeyEvent(Object source, int id, int code, char ch) { super(source, id); _code = code; _char = ch; }
    public int getKeyCode() { return _code; }
    public char getKeyChar() { return _char; }
    public String toString() {
        return pfxString()+",code="+_code+",char='"+_char+"')";
    }

    public static int getExtendedKeyCodeForChar(int c) {
        return DefaultKeymap.instance().mapChar((char)c);
    }
}
