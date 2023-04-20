package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class JButton extends JComponent {
    private String _text;
    private String _command;
    private boolean _hover;
    private boolean _hold;
    public JButton() { this(""); }    
    public JButton(String text) { _hover = _hold = false; setText(text); }
    public void addActionListener(ActionListener l) { addEventListener(l); }
    public void removeActionListener(ActionListener l) { removeEventListener(l); }
    public String getText() { return _text; }
    public void setText(String text) {
        _text = text;
        invalidate();
    }
    public String getActionCommand() { return _command; }
    public void setActionCommand(String command) {
        _command = command;
    }
    // prevent use as a container.. for now
    protected void addImpl(Component c, Object s, int i) {
        throw new IllegalArgumentException("cannot add components to JButton");
    }
    // accessors for derived classes
    protected boolean isHover() { return _hover; }
    protected boolean isHeld() { return _hold; }
    // set size based on text dimensions
    public Dimension getPreferredSize() {
        Point p;
        if (getText().length() > 0) {
            p = new Point(getFont().getFontMetrics().stringWidth(getText()), getFont().getFontMetrics().getHeight());
        } else {
            // not on screen yet or no text, default to smallish
            p = new Point(0,0);
        }
        return new Dimension(p._x+10, p._y+10);
    }
    public Dimension getMinimumSize() { return getPreferredSize(); }
    public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
    // process mouse events to show hover, click, release
    public void processEvent(AbstractEvent e) {
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
        for (EventListener l: _listeners)
            ((ActionListener)l).actionPerformed(a);
    }
    // paint a button!
    public void paintComponent(Graphics g) {
        if (_hold) {
            g.setColor(getForeground());
            g.fillRoundRect(2, 2, getWidth()-3, getHeight()-3, getWidth()/10, getHeight()/10);
            g.setColor(getBackground());
        } else {
            g.setColor(getForeground());
            g.fillRoundRect(2, 2, getWidth()-3, getHeight()-3, getWidth()/10, getHeight()/10);
            g.setColor(getBackground());
            int o = hasFocus() ? 2 : 1;
            g.fillRoundRect(2+o, 2+o, getWidth()-3-2*o, getHeight()-3-2*o, getWidth()/10, getHeight()/10);
            g.setColor(getForeground());
        }
        int w = g.getFont().getFontMetrics().stringWidth(getText());
        int h = g.getFont().getFontMetrics().getHeight();
        g.drawString(getText(), (getWidth()-w)/2, (getHeight()+h)/2);
        if (_hover)
            g.drawLine((getWidth()-w)/2, (getHeight()+h)/2, (getWidth()+w)/2, (getHeight()+h)/2);
    }
}
