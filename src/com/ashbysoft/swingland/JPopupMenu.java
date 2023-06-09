package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;
import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.ActionListener;
import com.ashbysoft.swingland.event.KeyEvent;

public class JPopupMenu extends Window implements ActionListener {
    public static final int TITLE_HEIGHT = 20;
    public static final int BORDER_WIDTH = 2;
    public JPopupMenu(Window owner) { this(owner, null); }
        public JPopupMenu(Window owner, String title) {
        super(owner, true);
        _log.info("<init>("+owner.getName()+")");
        setVisible(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setInsets(new Insets(title != null ? TITLE_HEIGHT : BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        if (title != null)
            setTitle(title);
    }
    // prevent general use of underlying container
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JPopupMenu");
    }
    public void add(JMenuItem item) {
        // hook into action events, so we can close ourselves
        // ignore JMenu components, as they select a sub-menu
        if (!(item instanceof JMenu))
            item.addActionListener(this);
        super.addImpl(item, null, -1);
    }
    public void add(JSeparator sep) {
        super.addImpl(sep, null, -1);
    }
    protected void processEvent(AbstractEvent e) {
        if (e instanceof KeyEvent) {
            KeyEvent k = (KeyEvent)e;
            // detect ESC key as close menu
            if (k.getID() == KeyEvent.KEY_RELEASED && k.getKeyCode() == KeyEvent.VK_ESC)
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, null));
            else if (k.getID() == KeyEvent.KEY_PRESSED) {
                // check other keys against mnemonics
                for (int i = 0; i < getComponentCount(); i += 1) {
                    Component c = getComponent(i);
                    if (c instanceof JMenuItem) {
                        JMenuItem m = (JMenuItem)c;
                        if (m.isEnabled() && m.getMnemonic() == k.getKeyCode()) {
                            k.consume();
                            m.fireActionPerformed(new ActionEvent(m, ActionEvent.ACTION_FIRED, m.getActionCommand()));
                            break;
                        }
                    }
                }
            }
        }
    }
    public void actionPerformed(ActionEvent e) {
        // grab owner ref before disposing everything
        Window o = getOwner();
        dispose();
        // now inform our owner if it's also a popup menu
        if (o instanceof JPopupMenu)
            ((JPopupMenu)o).actionPerformed(e);
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
        if (getTitle() != null) {
            g.setColor(getBackground());
            g.drawString(getTitle(), 2, TITLE_HEIGHT-2);
        }
        super.paint(g);
    }
}
