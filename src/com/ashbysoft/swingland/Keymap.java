package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.KeyEvent;

public interface Keymap {
    void modifiers(int depressed, int latched, int locked, int group);
    int getModifiersEx();
    KeyEvent mapCode(int keyCode);
    char mapUnmodified(int keyCode);
    int mapChar(char unicode);
}
