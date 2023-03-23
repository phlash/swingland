package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;
import java.util.ArrayList;

public class Container extends Component {
    private ArrayList<Component> _components;
    private LayoutManager _layoutManager;
    private Dimension _cachePrefSize;
    private Dimension _cacheMinSize;
    private Dimension _cacheMaxSize;
    private int _focusComponent;
    public Container() {
        _components = new ArrayList<Component>();
        _layoutManager = new BorderLayout();
        _focusComponent = -1;
    }
    public int getComponentCount() { return _components.size(); }
    public Component getComponent(int n) { return _components.get(n); }
    public Component add(Component c) { addImpl(c, null, -1); return c; }
    public Component add(Component c, int i) { addImpl(c, null, i); return c; }
    public void add(Component c, Object s) { addImpl(c, s, -1); }
    public void add(Component c, Object s, int i) { addImpl(c, s, i); }
    public Component add(String n, Component c) { addImpl(c, n, -1); return c; }
    protected void addImpl(Component c, Object s, int i) {
        // cannot add an ancestor, it would create a loop in the hierarchy!
        _log.info("add("+c.getName()+","+s+","+i+")");
        if (isAncestor(c))
            throw new IllegalArgumentException("Attempt to add an ancestor component to a container");
        // remove from elsewhere
        Container p = c.getParent();
        if (p != null)
            p.remove(c);
        // add to the component list
        if (i < 0)
            _components.add(c);
        else
            _components.add(i, c);
        // notify the component
        c.setParent(this);
        // notify the layout manager
        if (_layoutManager instanceof LayoutManager2)
            ((LayoutManager2)_layoutManager).addLayoutComponent(c, s);
        else if (s instanceof String)
            _layoutManager.addLayoutComponent((String)s, c);
        // set as focus component if first one
        if (_focusComponent < 0)
            _focusComponent = getComponentCount() - 1;
        invalidate();
    }
    private boolean isAncestor(Component c) {
        // is this us?
        if (this.equals(c))
            return true;
        // try parent (if any)
        Container p = getParent();
        if (p != null)
            return p.isAncestor(c);
        return false;
    }
    public void remove(int i) {
        _log.info("remove("+i+")");
        // remove from component list
        Component c = _components.remove(i);
        // notify layout manager
        _layoutManager.removeLayoutComponent(c);
        // notify component
        c.setParent(null);
        invalidate();
    }
    public void remove(Component c) {
        if (null==c)
            return;
        _log.info("remove("+c.getName()+")");
        int i = _components.indexOf(c);
        if (i >= 0)
            remove(i);
    }
    public LayoutManager getLayout() { return _layoutManager; }
    public void setLayout(LayoutManager lm) {
        _log.info("setLayout("+lm.getClass().getSimpleName()+")");
        _layoutManager = lm;
        invalidate();
    }
    public Component getFocusComponent() { return _focusComponent < 0 ? null : getComponent(_focusComponent); }
    public Component getComponentAt(int x, int y) {
        for (Component c: _components) {
            Rectangle b = c.getBounds();
            if (x >= b._x && y >= b._y && x < (b._x + b._w) && y < (b._y + b._h))
                return c;
        }
        return null;
    }
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (d != null)
            return d;
        if (null == _cachePrefSize)
            _cachePrefSize = _layoutManager.preferredLayoutSize(this);
        return _cachePrefSize;
    }
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (d != null)
            return d;
        if (null == _cacheMinSize)
            _cacheMinSize = _layoutManager.minimumLayoutSize(this);
        return _cacheMinSize;
    }
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (d != null)
            return d;
        if (null == _cacheMaxSize && _layoutManager instanceof LayoutManager2)
            _cacheMaxSize = ((LayoutManager2)_layoutManager).maximumLayoutSize(this);
        if (null == _cacheMaxSize)
            return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
        return _cacheMaxSize;
    }
    public void invalidate() {
        // ignore if we are already invalid
        if (!isValid())
            return;
        // invalidate out layout
        if (_layoutManager instanceof LayoutManager2)
            ((LayoutManager2)_layoutManager).invalidateLayout(this);
        // drop cached sizes
        _cachePrefSize = null;
        _cacheMinSize = null;
        _cacheMaxSize = null;
        // now invoke component behaviour
        super.invalidate();
    }
    public void validate() {
        // if we are not valid, validate down the component tree
        if (!isValid()) {
            validateTree();
            // now invoke component behaviour
            super.validate();
        }
    }
    protected void validateTree() {
        _log.info("validateTree()");
        // layout our components first..
        _layoutManager.layoutContainer(this);
        // validate component tree
        for (Component c: _components) {
            c.validate();
        }
    }

    public void dispatchEvent(AbstractEvent e) {
        // KeyEvents go to the currently focused component (if any)
        if (e instanceof KeyEvent) {
            Component c = getFocusComponent();
            if (c != null)
                c.dispatchEvent(e);
        // MouseEvents go to current component under pointer (if any)
        } else if (e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            Component c = getComponentAt(m.getX(), m.getY());
            if (c != null) {
                // translate event co-ordinates to target component
                Rectangle b = c.getBounds();
                c.dispatchEvent(new MouseEvent(c, m.getID(), m.getX() - b._x, m.getY() - b._y, m.getButton(), m.getState()));
            }
        }
        // now we pass back for default behaviour
        super.dispatchEvent(e);
    }

    public void paint(Graphics g) {
        if (!isVisible())
            return;
        // iterate them components!
        _log.info("Container:paint");
        for (Component c: _components) {
            g.setBounds(c.getBounds());
            c.paint(g);
        }
    }
}