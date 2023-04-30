package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class JButton extends JComponent implements SwingConstants {
    public static final int PAD = 5;
    // hand-drawn quarter circle radius 5 corner (BR)
    private static final int[][] corner = { {4,0}, {3,1}, {4,1}, {3,2}, {1,3}, {2,3}, {3,3}, {0,4}, {1,4} };
    private String _text;
    private int _mnemonic;
    private Icon _icon;
    private String _command;
    private int _hAlign;
    private int _hTextPos;
    private int _vAlign;
    private int _vTextPos;
    private boolean _hover;
    private boolean _hold;

    public JButton() { this(""); }    
    public JButton(String text) { this(text, null); }
    public JButton(Icon icon) { this("", icon); }
    public JButton(String text, Icon icon) {
        _log.info("<init>("+text+","+icon+")");
        _hover = _hold = false;
        if (text != null)
            setText(text);
        if (icon != null)
            setIcon(icon);
        _mnemonic = -1;
        _hAlign = CENTER;
        _hTextPos = TRAILING;
        _vAlign = CENTER;
        _vTextPos = CENTER;
    }
    public void addActionListener(ActionListener l) { addEventListener(l); }
    public void removeActionListener(ActionListener l) { removeEventListener(l); }
    public String getText() { return _text; }
    public void setText(String text) {
        _text = text;
        invalidate();
    }
    public Icon getIcon() { return _icon; }
    public void setIcon(Icon icon) {
        _icon = icon;
        invalidate();
    }
    public String getActionCommand() { return _command; }
    public void setActionCommand(String command) {
        _command = command;
    }
    public int getMnemonic() { return _mnemonic; }
    public void setMnemonic(int m) { _mnemonic = m; }
    public int getHorizontalAlignment() { return _hAlign; }
    public void setHorizontalAlignment(int a) { _hAlign = a; }
    public int getHorizontalTextPosition() { return _hTextPos; }
    public void setHorizontalTextPosition(int p) { _hTextPos = p; }
    public int getVerticalAlignment() { return _vAlign; }
    public void setVerticalAlignment(int a) { _vAlign = a; }
    public int getVerticalTextPosition() { return _vTextPos; }
    public void setVerticalTextPosition(int p) { _vTextPos = p; }
    // prevent use as a container.. for now
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JButton");
    }
    // accessors for derived classes
    protected boolean isHover() { return _hover; }
    protected boolean isHeld() { return _hold; }
    // set size based on text/icon dimensions
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet())
            return super.getPreferredSize();
        Dimension i;
        if (getIcon() != null)
            i = new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight());
        else
            i = new Dimension(0,0);
        Dimension t;
        if (getText().length() > 0)
            t = new Dimension(getFont().getFontMetrics().stringWidth(getText()), getFont().getFontMetrics().getHeight());
        else
            t = new Dimension(0,0);
        // combine icon and text dimensions according to alignment
        int w = switch (getHorizontalTextPosition()) {
                case CENTER -> i._w > t._w ? i._w : t._w;
                default -> i._w + t._w + PAD;
            };
        int h = switch (getVerticalTextPosition()) {
                case CENTER -> i._h > t._h ? i._h : t._h;
                default -> i._h + t._h + PAD;
            };
        return new Dimension(w+2*PAD, h+2*PAD);
    }
    public Dimension getMinimumSize() { return getPreferredSize(); }
    public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    // process events to show hover, click, release
    protected void processEvent(AbstractEvent e) {
        if (!isEnabled())
            return;
        _log.info("Jbutton:processEvent("+e.toString()+")");
        if (e instanceof MouseEvent) {
            MouseEvent m = (MouseEvent)e;
            if (m.getID() == MouseEvent.MOUSE_ENTERED) {
                _hover = true;
                repaint();
            } else if (m.getID() == MouseEvent.MOUSE_EXITED) {
                _hover = false;
                _hold = false;
                repaint();
            } else if (m.getID() == MouseEvent.MOUSE_BUTTON) {
                _hold = (m.getState() == MouseEvent.BUTTON_PRESSED);
                repaint();
            } else if (m.getID() == MouseEvent.MOUSE_CLICKED) {
                m.consume();
                fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, getActionCommand()));
                // directly dispatch action event to listeners
            }
        } else if (e instanceof KeyEvent) {
            KeyEvent k =(KeyEvent)e;
            if (KeyEvent.KEY_PRESSED == k.getID() && KeyEvent.VK_SPACE == k.getKeyCode()) {
                _hold = true;
                repaint();
            } else if (KeyEvent.KEY_RELEASED == k.getID() && KeyEvent.VK_SPACE == k.getKeyCode()) {
                _hold = false;
                repaint();
            } else if (KeyEvent.KEY_TYPED == k.getID() && ' ' == k.getKeyChar()) {
                k.consume();
                fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, getActionCommand()));
            }
        }
    }
    protected void fireActionPerformed(ActionEvent a) {
        _log.info("JButton:fireActionPerformed("+a.toString()+")");
        for (EventListener l: _listeners)
            ((ActionListener)l).actionPerformed(a);
    }
    // intercept setEnabled to clear display states
    public void setEnabled(boolean e) {
        super.setEnabled(e);
        _hold = _hover = false;
        repaint();
    }
    // mnemonic helper, returns x offsets of underlined char, or null
    protected int[] findMnemonic(Graphics g) {
        if (getMnemonic() < 0)
            return null;
        int[] rv = { -1,-1 };
        int s = 0;
        FontMetrics fm = g.getFont().getFontMetrics();
        for (int o = 0; o < _text.length(); o += 1) {
            int cp = _text.codePointAt(o);
            int l = fm.charWidth(cp);
            if (KeyEvent.getExtendedKeyCodeForChar(_text.charAt(o)) == getMnemonic()) {
                rv[0] = s;
                rv[1] = s + l;
                return rv;
            }
            s += l;
        }
        return null;
    }
    // paint a button!
    private void fillCornerRect(Graphics g, int x, int y, int w, int h) {
        int lx = x + PAD;
        int lw = w - 2*PAD;
        int ty = y + PAD;
        int lh = h - 2*PAD;
        g.fillRect(lx, y, lw, PAD);
        g.fillRect(lx, ty+lh, lw, PAD);
        g.fillRect(x, ty, w, lh);
        for (int[] xy : corner) {
            g.drawLine(lx+lw, ty+lh+xy[1], lx+lw+xy[0], ty+lh+xy[1]);   // BR
            g.drawLine(lx+lw, ty-1-xy[1], lx+lw+xy[0], ty-1-xy[1]);     // TR
            g.drawLine(lx-1-xy[0], ty+lh+xy[1], lx, ty+lh+xy[1]);   // BL
            g.drawLine(lx-1-xy[0], ty-1-xy[1], lx, ty-1-xy[1]);     // TL
        }
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color fg = isEnabled() ? getForeground() : Window.DEFAULT_DISABLED;
        g.setColor(fg);
        // draw button frame (rounded rectangle, unless too small)
        if (Math.min(getWidth(), getHeight()) < 4*PAD) {
            if (_hold) {
                g.fillRect(2, 2, getWidth()-3, getHeight()-3);
                g.setColor(getBackground());
            } else {
                g.fillRect(2, 2, getWidth()-3, getHeight()-3);
                g.setColor(getBackground());
                int o = hasFocus() ? 2 : 1;
                g.fillRect(2+o, 2+o, getWidth()-3-2*o, getHeight()-3-2*o);
                g.setColor(fg);
            }
        } else {
            if (_hold) {
                fillCornerRect(g, 2, 2, getWidth()-3, getHeight()-3);
                g.setColor(getBackground());
            } else {
                fillCornerRect(g, 2, 2, getWidth()-3, getHeight()-3);
                g.setColor(getBackground());
                int o = hasFocus() ? 2 : 1;
                fillCornerRect(g, 2+o, 2+o, getWidth()-3-2*o, getHeight()-3-2*o);
                g.setColor(fg);
            }
        }
        // get content dimensions
        int iw = getIcon() != null ? getIcon().getIconWidth() : 0;
        int ih = getIcon() != null ? getIcon().getIconHeight() : 0;
        int tw = getText().length() > 0 ? g.getFont().getFontMetrics().stringWidth(getText()) : 0;
        int th = getText().length() > 0 ? g.getFont().getFontMetrics().getHeight() : 0;

        // calculate drawing size, then residual space according to text position (may be zero)
        int dw = switch (getHorizontalTextPosition()) {
            case CENTER -> Math.max(iw, tw);
            default -> iw + tw + PAD;
        };
        int rw = getWidth() - dw;
        int dh = switch (getVerticalTextPosition()) {
            case CENTER -> Math.max(ih, th);
            default -> ih + th;
        };
        int rh = getHeight() - dh;
        // distribute residual space, derive padding from 0,0
        int px = switch (getHorizontalAlignment()) {
            case LEFT -> PAD;
            case RIGHT -> rw - PAD;
            default -> rw / 2;
        };
        int py = switch (getVerticalAlignment()) {
            case TOP -> PAD;
            case BOTTOM -> rh - PAD;
            default -> rh / 2;
        };
        // draw icon first..
        if (getIcon() != null) {
            int x = switch (getHorizontalTextPosition()) {
                case LEFT, LEADING -> px + dw - iw;
                case RIGHT, TRAILING -> px;
                default -> px + (dw - iw) / 2;
            };
            int y = switch (getVerticalTextPosition()) {
                case TOP -> py + dh - ih;
                case BOTTOM -> py;
                default -> py + (dh - ih) / 2;
            };
            getIcon().paintIcon(this, g, x, y);
        }
        // now text (which may overlap icon)
        if (getText().length() > 0) {
            int x = switch (getHorizontalTextPosition()) {
                case LEFT, LEADING -> px;
                case RIGHT, TRAILING -> px + dw - tw;
                default -> px + (dw - tw) / 2;
            };
            int y = switch (getVerticalTextPosition()) {
                case TOP -> py + th;
                case BOTTOM -> py + dh;
                default -> py + (dh + th) / 2;
            };
            g.drawString(getText(), x, y);
            if (_hover)
                g.drawLine(x, y, x + tw, y);
            int[] mx = findMnemonic(g);
            if (mx != null)
                g.drawLine(x + mx[0], y, x + mx[1], y);
        }
    }
}
