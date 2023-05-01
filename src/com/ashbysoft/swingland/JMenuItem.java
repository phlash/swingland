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
            return super.getText() + " [" + _accelerator.toString() + "]";
        return super.getText();
    }
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
        int w = g.getFont().getFontMetrics().stringWidth(getText());
        int h = g.getFont().getFontMetrics().getHeight();
        int x = (getWidth()-w)/2;
        int y = (getHeight()+h)/2;
        g.drawString(getText(), x, y);
        int[] mx = findMnemonic(g);
        if (mx != null)
            g.drawLine(x + mx[0], y, x + mx[1], y);
    }
}
