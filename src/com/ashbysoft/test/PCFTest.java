package com.ashbysoft.test;

import com.ashbysoft.swingland.Color;
import com.ashbysoft.swingland.Font;
import com.ashbysoft.swingland.FontMetrics;
import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.JComponent;
import com.ashbysoft.swingland.JFrame;
import com.ashbysoft.swingland.SwingUtilities;

public class PCFTest extends JComponent implements Runnable {
    public static void main(String[] args) {
        var us = new PCFTest();
        us.config(args);
        SwingUtilities.invokeLater(us);
    }
    private String[] fontNames;
    private Color[] colours = { Color.BLACK, Color.BLUE, Color.DARK_GRAY, Color.MAGENTA };
    private void config(String[] args) {
        fontNames = args;
    }
    public void run() {
        // create GUI
        setBackground(Color.LIGHT_GRAY);
        JFrame frame = new JFrame("PCF font test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    public void paintComponent(Graphics g) {
        // iterate those fonts..
        int y = 0;
        int i = 0;
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        for (var f : fontNames) {
            Font fn = Font.getFont(f);
            FontMetrics fm = fn.getFontMetrics();
            if (null == fm) {
                _log.error("Unable to load font: "+f);
                continue;
            }
            g.setFont(fn);
            g.setColor(colours[i]);
            i = (i + 1) % colours.length;
            y += fm.getHeight();
            if (y >= getHeight())
                break;
            String s = fn.getFontName() + ": ";
            int x = fm.stringWidth(s);
            g.drawString(s, 0, y);
            for (int cp = 0; cp <= 0xffff; cp += 1) {
                if (fn.canDisplay(cp)) {
                    char[] ch = { (char)cp };
                    g.drawChars(ch, 0, 1, x, y);
                    x += fm.charWidth(cp);
                    if (x >= getWidth())
                        break;
                }
            }
        }
    }
}
