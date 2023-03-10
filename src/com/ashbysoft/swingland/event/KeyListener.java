package com.ashbysoft.swingland.event;

public interface KeyListener extends EventListener {
    void keyPressed(KeyEvent e);
    void keyReleased(KeyEvent e);
    void keyTyped(KeyEvent e);
}
