package com.ashbysoft.swingland;

import java.util.ArrayList;
import com.ashbysoft.swingland.event.ActionEvent;

// a menu item that holds a sub-menu and pops it up when activated

public class JMenu extends JMenuItem {
    private ArrayList<JMenuItem> _items;
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
        super.fireActionPerformed(a);
        if (_items.size() == 0)
            return;
        Component c = getParent();
        while (c != null && !(c instanceof Window))
            c = c.getParent();
        if (null == c) {
            _log.error("missing Window parent");
            return;
        }
        JPopupMenu pop = new JPopupMenu((Window)c);
        for (var item : _items)
            pop.add(item);
        pop.setForeground(getForeground());
        pop.setBackground(getBackground());
        pop.setFont(getFont());
        pop.setCursor(getCursor());
        pop.setVisible(true);
    }
}
