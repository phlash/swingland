package com.ashbysoft.test;

import java.util.ArrayList;

import com.ashbysoft.swingland.Color;
import com.ashbysoft.swingland.Font;
import com.ashbysoft.swingland.FontMetrics;
import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.JComponent;
import com.ashbysoft.swingland.JFrame;
import com.ashbysoft.swingland.SwingUtilities;
import com.ashbysoft.swingland.geom.AffineTransform;

public class FontTest extends JComponent implements Runnable {
    public static void main(String[] args) {
        var us = new FontTest();
        us.config(args);
        SwingUtilities.invokeLater(us);
    }
    private ArrayList<String> fontNames = new ArrayList<>();
    private Color[] colours = { Color.BLACK, Color.BLUE, Color.DARK_GRAY, Color.MAGENTA };
    private boolean doRotate = false;
    private void config(String[] args) {
        for (var s: args) {
            if (s.startsWith("-r"))
                doRotate = true;
            else if (s.startsWith("-h") || s.startsWith("--h")) {
                _log.error("usage: FontTest [-r] [-h] <font name / path> [...]");
                System.exit(0);
            } else
                fontNames.add(s);
        }
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
        int i = 0;
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        double rotation = 0.0;
        double rinc = (Math.PI * 2.0)/(double)fontNames.size();
        int y = doRotate ? getHeight()/2 : 0;
        for (var f : fontNames) {
            Font fn = Font.getFont(f);
            FontMetrics fm = g.getFontMetrics(fn);
            if (null == fm) {
                _log.error("Unable to load font: "+f);
                continue;
            }

            if (doRotate) {
                AffineTransform tr = new AffineTransform();
                tr.rotate(rotation);
                rotation += rinc;
                fn = fn.deriveFont(tr);
            } else {
                y += fm.getHeight();
            }
            g.setFont(fn);
            g.setColor(colours[i]);
            i = (i + 1) % colours.length;
            StringBuffer sb = new StringBuffer(fn.getFontName()).append(": ");
            int w = fm.stringWidth(sb.toString());
            int m = getWidth()*getWidth()/4 + getHeight()*getHeight()/4;
            int x = doRotate ? getWidth()/2 : 0;
            for (int cp = 0; cp <= 0xffff; cp += 1) {
                if (fn.canDisplay(cp)) {
                    sb.append((char)cp);
                    w += fm.charWidth(cp);
                    if (doRotate && w*w > m)
                        break;
                    if (!doRotate && x+w > getWidth())
                        break;
                }
            }
            g.drawString(sb.toString(), x, y);
        }
    }
}
