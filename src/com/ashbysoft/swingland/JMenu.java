package com.ashbysoft.swingland;

import java.util.ArrayList;
import com.ashbysoft.swingland.event.ActionEvent;

// a menu item that holds a sub-menu and pops it up when activated

public class JMenu extends JMenuItem {
    private ArrayList<JMenuItem> _items;
    private JPopupMenu _popup;
    public JMenu() { this(""); }
    public JMenu(String text) {
        super(text);
        _items = new ArrayList<>();
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
        // process all button behaviour (may adjust menu items, etc.)
        super.fireActionPerformed(a);
        // if we already have a popup reference, and it's not disposed (no owner), stop here
        // also stop if we have no items to put in a menu!
        if ((_popup != null && _popup.getOwner() != null) || _items.size() == 0)
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
        _popup = new JPopupMenu((Window)c);
        for (var item : _items)
        _popup.add(item);
        _popup.setBackground(getBackground());
        _popup.setForeground(getForeground());
        _popup.setFont(getFont());
        _popup.setCursor(getCursor());
        // place popup menu directly below us
        _popup.setLocation(new Point(pos._x, pos._y + getHeight()));
        _popup.setVisible(true);
    }
}
