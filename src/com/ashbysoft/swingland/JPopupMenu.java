package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.ActionListener;

public class JPopupMenu extends Window implements ActionListener {
    public static final int BORDER_WIDTH = 2;
    private JMenu _parent;
    public JPopupMenu(Window owner) { this(owner, null); }
    public JPopupMenu(Window owner, JMenu parent) {
        super(owner, true);
        _log.info("<init>("+owner.getName()+","+(parent != null ? parent.getName() : "null")+")");
        _parent = parent;
        setVisible(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setInsets(new Insets(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    }
    // prevent general use of underlying container
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JPopupMenu");
    }
    public void add(JMenuItem item) {
        // hook into action events, so we can close ourselves
        item.addActionListener(this);
        super.addImpl(item, null, -1);
    }
    public void actionPerformed(ActionEvent e) {
        // a menu item was clicked.. we're done
        dispose();
        // if we have a parent menu, let it know
        if (_parent != null)
            _parent.fireActionPerformed(null);
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
