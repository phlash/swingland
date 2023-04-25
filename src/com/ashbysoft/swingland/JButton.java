package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class JButton extends JComponent implements SwingConstants {
    public static final int PAD = 5;

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
    // process mouse events to show hover, click, release
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
            } else if (KeyEvent.KEY_TYPED == k.getID() && ' ' == k.getKeyChar())
                fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, getActionCommand()));
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
    // paint a button!
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cw = getWidth() > getHeight() ? getHeight()/4 : getWidth()/4;
        if (cw > 25) cw = 25;
        if (_hold) {
            g.setColor(isEnabled() ? getForeground() : Window.DEFAULT_DISABLED);
            g.fillRoundRect(2, 2, getWidth()-3, getHeight()-3, cw, cw);
            g.setColor(getBackground());
        } else {
            g.setColor(isEnabled() ? getForeground() : Window.DEFAULT_DISABLED);
            g.fillRoundRect(2, 2, getWidth()-3, getHeight()-3, cw, cw);
            g.setColor(getBackground());
            int o = hasFocus() ? 2 : 1;
            g.fillRoundRect(2+o, 2+o, getWidth()-3-2*o, getHeight()-3-2*o, cw, cw);
            g.setColor(isEnabled() ? getForeground() : Window.DEFAULT_DISABLED);
        }
        // we always draw an icon if we have one..
        Icon i = getIcon();
        if (i != null) {
            int ix = switch (getHorizontalTextPosition()) {
                case LEFT, LEADING -> getWidth() - i.getIconWidth() - PAD;
                case RIGHT, TRAILING -> PAD;
                default -> switch (getHorizontalAlignment()) {
                    case LEFT -> PAD;
                    case RIGHT -> getWidth() - i.getIconWidth() - PAD;
                    default -> (getWidth() - i.getIconWidth()) / 2;
                };
            };
            int iy = switch (getVerticalTextPosition()) {
                case TOP -> getHeight() - i.getIconHeight() - PAD;
                case BOTTOM -> PAD;
                default -> switch (getVerticalAlignment()) {
                    case TOP -> PAD;
                    case BOTTOM -> getHeight() - i.getIconHeight() - PAD;
                    default -> (getHeight() - i.getIconHeight()) / 2;
                };
            };
            i.paintIcon(this, g, ix, iy);
        }
        if (getText().length() > 0) {
            // grab text dimensions
            int w = g.getFont().getFontMetrics().stringWidth(getText());
            int h = g.getFont().getFontMetrics().getHeight();
            // position the text according to alignment
            int tx = switch (getHorizontalTextPosition()) {
                case LEFT, LEADING -> PAD;
                case RIGHT, TRAILING -> getWidth() - w - PAD;
                default -> switch (getHorizontalAlignment()) {
                    case LEFT -> PAD;
                    case RIGHT -> getWidth() - w - PAD;
                    default -> (getWidth() - w) / 2;
                };
            };
            int ty = switch (getVerticalTextPosition()) {
                case TOP -> PAD + h;
                case BOTTOM -> getHeight() - PAD;
                default -> switch (getVerticalAlignment()) {
                    case TOP -> PAD + h;
                    case BOTTOM -> getHeight() - PAD;
                    default -> (getHeight() + h) / 2;
                };
            };
            g.drawString(getText(), tx, ty);
            if (_hover)
                g.drawLine(tx, ty, tx + w, ty);
        }
    }
}
