package com.ashbysoft.swingland;

import java.util.ArrayList;
import com.ashbysoft.swingland.event.ActionEvent;

// a menu item that holds a sub-menu and pops it up when activated

public class JMenu extends JMenuItem {
    private ArrayList<JMenuItem> _items;
    private boolean _isActive;
    public JMenu() { this(""); }
    public JMenu(String text) {
        super(text);
        _items = new ArrayList<>();
        _isActive = false;
    }
    public void add(JMenuItem item) {
        _items.add(item);
    }
    public void remove(JMenuItem item) {
        _items.remove(item);
    }
    public void removeAll() {
        _items.clear();
    }
    // post-intercept action performed, so we can pop up our sub-menu
    protected void fireActionPerformed(ActionEvent a) {
        // special contract with JPopupMenu to clear our state
        if (null == a) {
            _isActive = false;
            return;
        }
        super.fireActionPerformed(a);
        if (a.isConsumed() || _items.size() == 0 || _isActive)
            return;
        _log.info("JMenu:fireActionPerformed("+a.toString()+")");
        // find our Window ancestor, and calculate our position relative to it
        Rectangle pos = getBounds();
        Component c = this;
        while (c != null && !(c instanceof Window)) {
            c = c.getParent();
            pos = pos.offset(c.getBounds());
        }
        if (null == c) {
            _log.error("missing Window parent");
            return;
        }
        JPopupMenu pop = new JPopupMenu((Window)c, this);
        for (var item : _items)
            pop.add(item);
        pop.setBackground(getBackground());
        pop.setForeground(getForeground());
        pop.setFont(getFont());
        pop.setCursor(getCursor());
        // place popup menu directly below us
        pop.setLocation(new Point(pos._x, pos._y + getHeight()));
        pop.setVisible(true);
        _isActive = true;
    }
}
