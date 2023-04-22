package com.ashbysoft.swingland;

import java.util.ArrayList;
import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.WindowEvent;
import com.ashbysoft.swingland.event.WindowListener;

// a menu item that holds a sub-menu and pops it up when activated

public class JMenu extends JMenuItem implements WindowListener {
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
        // if we still have a popup reference, stop here
        // also stop if we have no items to put in a menu!
        if (_popup != null || _items.size() == 0)
            return;
        _log.info("JMenu:fireActionPerformed("+a.toString()+")");
        // find our Window ancestor, and calculate our position relative to it
        Rectangle pos = getBounds();
        Component c = getParent();
        while (c != null && !(c instanceof Window)) {
            pos = pos.offset(c.getBounds());
            c = c.getParent();
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
        // place popup menu directly below us if our parent is a menu bar, otherwise to the right
        Container p =getParent();
        if (p instanceof JMenuBar)
            _popup.setLocation(new Point(pos._x, pos._y + getHeight()));
        else
            _popup.setLocation(new Point(pos._x + getWidth(), pos._y));
        // subscribe all JMenu instances that share our parent container, so they see our popup open/close (incluing us!)
        for (int i = 0; i < p.getComponentCount(); i += 1) {
            c = p.getComponent(i);
            if (c instanceof JMenu)
                _popup.addWindowListener((JMenu)c);
        }
        _popup.setVisible(true);
    }
    public void windowOpened(WindowEvent w) {
        // if another popup got opened, close ours
        if (_popup != null && !_popup.equals(w.getSource()))
            _popup.dispose();
    }
    public void windowClosing(WindowEvent w) {}
    public void windowClosed(WindowEvent w) {
        // if our popup got closed, drop our reference
        if (_popup != null && _popup.equals(w.getSource()))
            _popup = null;
    }
}
