package com.ashbysoft.swingland;

// a button, with a simpler appearance for use in menus

public class JMenuItem extends JButton {
    private KeyStroke _accelerator;
    public JMenuItem() { this(""); }
    public JMenuItem(String text) {
        super(text);
        _log.info("<init>("+text+")");
        _accelerator = null;
    }
    public KeyStroke getAccelerator() { return _accelerator; }
    public void setAccelerator(KeyStroke a) { _accelerator = a; }
    public String getText() {
        if (_accelerator != null)
            return super.getText() + getAcceleratorText();
        return super.getText();
    }
    private String getAcceleratorText() { return (_accelerator != null) ? " [" + _accelerator.toString() + "]" : ""; }
    public void paintComponent(Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.setColor(isEnabled() ? getForeground() : Window.DEFAULT_DISABLED);
        if (isHover()) {
            g.fillRect(2, 2, getWidth()-3, getHeight()-3);
            g.setColor(getBackground());
        }
        String t1 = super.getText();
        String t2 = getAcceleratorText();
        FontMetrics fm = g.getFont().getFontMetrics();
        int w2 = fm.stringWidth(t2);
        int h = fm.getHeight();
        int y = (getHeight()+h)/2;
        g.drawString(t1, PAD, y);
        if (t2.length() > 0) {
            g.setColor(Window.DEFAULT_DISABLED);
            g.drawString(t2, getWidth() - w2 - PAD, y);
        }
        int[] mx = findMnemonic(g);
        if (mx != null)
            g.drawLine(PAD + mx[0], y, PAD + mx[1], y);
    }
}
