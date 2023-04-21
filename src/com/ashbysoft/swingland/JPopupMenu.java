package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;
import com.ashbysoft.swingland.event.KeyEvent;
import com.ashbysoft.swingland.event.MouseEvent;

public class JPopupMenu extends Window {
    public static final int BORDER_WIDTH = 2;
    public JPopupMenu(Window owner) {
        super(owner, true);
        _log.info("<init>("+owner.getName()+")");
        setVisible(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setInsets(new Insets(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    }
    // prevent general use of underlying container
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JPopupMenu");
    }
    public void add(JMenuItem item) {
        super.addImpl(item, null, -1);
    }
    public void processEvent(AbstractEvent e) {
        // We nuke ourselves if the mouse exits, or ESC is pressed
        boolean done = false;
        if (e instanceof KeyEvent) {
            KeyEvent k = (KeyEvent)e;
            if (k.getID() == KeyEvent.KEY_PRESSED && k.getKeyCode() == KeyEvent.VK_ESC) {
                k.consume();
                done = true;
            }
        } else if (e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            if (m.getID() == MouseEvent.MOUSE_EXITED) {
                m.consume();
                done = true;
            }
        }
        if (done)
            dispose();
    }
    public void paint(Graphics g) {
        if (!isVisible())
            return;
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getForeground());
        Insets m = getInsets();
        g.fillRect(0, 0, getWidth(), m._t);
        g.fillRect(0, m._t, m._l, getHeight()-m._b);
        g.fillRect(getWidth()-m._r, m._t, m._r, getHeight()-m._b);
        g.fillRect(0, getHeight()-m._b, getWidth(), m._b);
        super.paint(g);
    }
}
