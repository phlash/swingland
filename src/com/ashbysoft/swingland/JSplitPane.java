package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.AbstractEvent;
import com.ashbysoft.swingland.event.MouseEvent;

public class JSplitPane extends JComponent {
    public static final int VERTICAL_SPLIT = 0;
    public static final int HORIZONTAL_SPLIT = 1;
    public static final int DEFAULT_DIVIDER_SIZE = 5;
    public static final double DEFAULT_RESIZE_WEIGHT = 0.0;

    private int _orientation;
    private boolean _continuous;
    private Component _left;
    private Component _right;
    private Cursor _hover;
    private Cursor _drag;
    private int _offset;
    private int _minpos;
    private int _maxpos;
    private int _divpos;
    private int _divsize;
    private double _resizeWeight;

    public JSplitPane() { this(HORIZONTAL_SPLIT); }
    public JSplitPane(int o) { this(o, false); }
    public JSplitPane(int o, Component l, Component r) { this(o, false, l, r); }
    public JSplitPane(int o, boolean c) { this(o, c, new JButton("Left"), new JButton("Right")); }
    public JSplitPane(int o, boolean c, Component l, Component r) {
        _log.info("<init>("+o+","+c+","+l+","+r+")");
        setLayout(new NullLayout());
        _orientation = o;
        _continuous = c;
        setLeftComponent(l);
        setRightComponent(r);
        _divpos = 0;
        _offset = -1;
        _divsize = DEFAULT_DIVIDER_SIZE;
        _resizeWeight = DEFAULT_RESIZE_WEIGHT;
        _hover = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        _drag = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    public int getOrientation() { return _orientation; }
    public void setOrientation(int o) { _orientation = o; refresh(); }
    public boolean isContinuousLayout() { return _continuous; }
    public void setContinuousLayout(boolean c) { _continuous = c; }
    public Component getLeftComponent() { return _left; }
    public Component getTopComponent() { return getLeftComponent(); }
    public Component getRightComponent() { return _right; }
    public Component getBottomComponent() { return getRightComponent(); }
    public void setLeftComponent(Component l) {
        _log.info("setLeftComponent("+l+")");
        remove(0);
        add(l, null, 0);
        _left = l;
    }
    public void setTopComponent(Component t) { setLeftComponent(t); }
    public void setRightComponent(Component r) {
        _log.info("setRightComponent("+r+")");
        remove(1);
        add(r, null, -1);
        _right = r;
    }
    public void setBottomComponent(Component b) { setRightComponent(b); }
    protected void addImpl(Component c, Object s, int i) {
        if (getComponentCount() < 2)
            super.addImpl(c, s, i);
        else
            throw new IllegalArgumentException("JSplitPane: cannot add > 2 components to pane");
    }
    public int getDividerLocation() { return _divpos; }
    public void setDividerLocation(double f) {
        int p = HORIZONTAL_SPLIT == _orientation ? (int)((double)getWidth() * f) : (int)((double)getHeight() * f);
        setDividerLocation(p);
    }
    public void setDividerLocation(int p) {
        // only move if we are valid & don't violate minimum sizes
        if (!isValid())
            return;
        if (p >= _minpos && p <= _maxpos) {
            _divpos = p;
            layoutSplit();
            repaint();
        }
    }
    public int getDividerSize() { return _divsize; }
    public void setDividerSize(int s) {
        _divsize = s;
        refresh();
    }
    public double getResizeWeight() { return _resizeWeight; }
    public void setResizeWeight(double w) { _resizeWeight = w; refresh(); }
    public Dimension getPreferredSize() { return getMinimumSize(); }
    public Dimension getMinimumSize() {
        // base our minimum size on components, plus insets/border and divider
        Dimension ld = _left.getMinimumSize();
        Dimension rd = _right.getMinimumSize();
        Insets ins = getInsets();
        int w = HORIZONTAL_SPLIT == _orientation ? ld._w + rd._w + ins._l + ins._r + _divsize : Math.max(ld._w, rd._w) + ins._l + ins._r;
        int h = HORIZONTAL_SPLIT == _orientation ? Math.max(ld._h, rd._h) + ins._t + ins._b : ld._h + rd._h + ins._t + ins._b + _divsize;
        return new Dimension(w, h);
    }
    // shared left/right layout based on divider position
    private void layoutSplit() {
        Insets ins = getInsets();
        if (HORIZONTAL_SPLIT == _orientation) {
            _left.setBounds(ins._l, ins._t, _divpos - ins._l, getHeight() - ins._t - ins._b);
            _right.setBounds(_divpos + _divsize, ins._t, getWidth() - ins._r - _divpos - _divsize, getHeight() - ins._t - ins._b);
        } else {
            _left.setBounds(ins._l, ins._t, getWidth() - ins._l - ins._r, _divpos - ins._t);
            _right.setBounds(ins._l, _divpos + _divsize, getWidth() - ins._l - ins._r, getHeight() - ins._b - _divpos - _divsize);
        }
        _left.validate();
        _right.validate();
    }
    // override standard layout manager interaction
    public boolean isValidateRoot() { return true; }
    protected void validateTree() {
        // layout our components, honour their minimum size, leaving room for the insets/border and divider
        // NB: we are only called when invalid, thus a user set divider position does not apply..
        Dimension ld = _left.getMinimumSize();
        Dimension rd = _right.getMinimumSize();
        Insets ins = getInsets();
        _minpos = HORIZONTAL_SPLIT == _orientation ? ins._l + ld._w : ins._t + ld._h;
        _maxpos = HORIZONTAL_SPLIT == _orientation ? getWidth() - ins._r - rd._w - _divsize : getHeight() - ins._b - rd._h - _divsize;
        // distribute remaining distance (if any) according to resizeWeight
        int rem = HORIZONTAL_SPLIT == _orientation ?
            getWidth() - ld._w - rd._w - ins._l - ins._r - _divsize :
            getHeight() - ld._h - rd._h - ins._t - ins._b - _divsize;
        rem = rem > 0 ? (int)((double)rem * _resizeWeight) : 0;
        _divpos = HORIZONTAL_SPLIT == _orientation ? ins._l + ld._w + rem : ins._t + ld._h + rem;
        layoutSplit();
    }
    // mouse interaction
    protected void processEvent(AbstractEvent e) {
        super.processEvent(e);
        if (e.isConsumed() || !(e instanceof MouseEvent))
            return;
        MouseEvent m = (MouseEvent)e;
        int p = HORIZONTAL_SPLIT == _orientation ? m.getX() : m.getY();
        if (p >= _divpos && p < _divpos + _divsize) {
            // within the divider bar
            if (m.getID() == MouseEvent.MOUSE_BUTTON && m.getState() == MouseEvent.BUTTON_PRESSED) {
                // button down, set drag cursor, save offset
                setCursor(_drag);
                drawCursor(this);
                _offset = p - _divpos;
                e.consume();
                return;
            } else if (m.getID() == MouseEvent.MOUSE_MOVE || m.getID() == MouseEvent.MOUSE_BUTTON) {
                // move or button change (thus not dragging), set hover cursor, clear drag offset
                setCursor(_hover);
                drawCursor(this);
                _offset = -1;
                e.consume();
                return;
            }
        } else if (_offset < 0) {
            // outside divider bar, not dragging, restore cursor
            setCursor(null);
            drawCursor(this);
        }
        if (m.getID() == MouseEvent.MOUSE_DRAGGED && _offset >= 0) {
            // dragging, track mouse with divider within limits
            setDividerLocation(p + _offset);
            e.consume();
        } else if (m.getID() == MouseEvent.MOUSE_MOVE || m.getID() == MouseEvent.MOUSE_BUTTON) {
            // move or button (thus not dragging), reset cursor and clear drag offset
            setCursor(null);
            drawCursor(this);
            _offset = -1;
        }
    }
    // paint the divider..
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getForeground());
        Insets ins = getInsets();
        if (HORIZONTAL_SPLIT == _orientation)
            g.fillRect(_divpos, ins._t, _divsize, getHeight() - ins._t - ins._b);
        else
            g.fillRect(ins._l, _divpos, getWidth() - ins._l - ins._r, _divsize);
    }
}
