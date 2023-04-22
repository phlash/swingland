package com.ashbysoft.swingland;

// a button, with a simpler appearance for use in menus

public class JMenuItem extends JButton {
    public JMenuItem() { this(""); }
    public JMenuItem(String text) {
        super(text);
        _log.info("<init>("+text+")");
    }
    public void paintComponent(Graphics g) {
        if (isBackgroundSet()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.setColor(isEnabled() ? getForeground() : Window.DEFAULT_DISABLED);
        if (isHeld()) {
            g.fillRect(2, 2, getWidth()-3, getHeight()-3);
            g.setColor(getBackground());
        }
        int w = g.getFont().getFontMetrics().stringWidth(getText());
        int h = g.getFont().getFontMetrics().getHeight();
        g.drawString(getText(), (getWidth()-w)/2, (getHeight()+h)/2);
        if (isHover())
            g.drawLine((getWidth()-w)/2, (getHeight()+h)/2, (getWidth()+w)/2, (getHeight()+h)/2);
    }
}
