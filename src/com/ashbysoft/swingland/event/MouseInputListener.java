package com.ashbysoft.swingland.event;

public interface MouseInputListener extends EventListener {
    void mouseClicked(MouseEvent e);
    void mouseEntered(MouseEvent e);
    void mouseExited(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseMoved(MouseEvent e);
}
