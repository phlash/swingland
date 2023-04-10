package com.ashbysoft.fed;

import java.io.File;

// Font EDitor..
/*
 *  +---------------------------------------------------------------+
 *  | | < | Selectable display of all glyphs. | > | Add | Remove |  |
 *  +---------------------------------------------------------------+
 *  |  Zoomed pixel display of selected glyph, editable.            |
 *  |  Left button sets pixel.                                      |
 *  |  Right button clears pixel.                                   |
 *  |  Saves are automatic.                                         |
 *  +---------------------------------------------------------------+
 */
import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.BorderLayout;
import com.ashbysoft.swingland.Color;
import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.Frame;
import com.ashbysoft.swingland.JButton;
import com.ashbysoft.swingland.JComponent;
import com.ashbysoft.swingland.JFrame;
import com.ashbysoft.swingland.JPanel;
import com.ashbysoft.swingland.SwingUtilities;
import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.ActionListener;
import com.ashbysoft.swingland.event.KeyEvent;
import com.ashbysoft.swingland.event.KeyListener;

public class Fed implements ActionListener, KeyListener, Runnable {
    public static void main(String[] args) {
        System.out.println("--- (Ashbysoft *) Swingland font editor ---");
        new Fed().boot(args);
    }

    private Logger _log = new Logger("[Fed]:");
    private EditableFont _font;
    private JFrame _frame;

    public void boot(String[] args) {
        String name = null;
        int width = 8;
        int height = 16;
        int baseline = 3;
        int leading = 3;
        int offset = 0;
        int missing = 0;
        for (int a = 0; a < args.length; a += 1) {
            if (args[a].startsWith("-f"))
                name = args[++a];
            else {
            }
        }
        if (null == name) {
            System.out.println("usage: fed -f <font file> [-w <glyph width:8>] [-h <glyph height:16>] [-b <glyph baseline:3>] [-l <glyph leading:3>]");
            System.out.println("[-o <unicode offset:0>] [-m <missing glyph:0>]");
            return;
        }
        _font = new EditableFont(_log, new File(name), width, height, baseline, leading, offset, missing);
        SwingUtilities.invokeLater(this);
    }
    public void run() {
        // build the UI
        _frame = new JFrame("Fed: "+_font.getFontName());
        _frame.setLayout(new BorderLayout());
        // fix frame size, set border layout for content pane
        _frame.setSize(800, 800);
        //_frame.setLayout(new BorderLayout());
        _frame.setBackground(Color.CYAN);
        _frame.setForeground(Color.MAGENTA);
        _frame.addKeyListener(this);
        // panel to hold top row of controls
        JPanel top = new JPanel(new BorderLayout());
        _frame.add(top, BorderLayout.NORTH);
        // controls in the top panel
        JButton left = new JButton("<");
        left.setActionCommand("left");
        left.addActionListener(this);
        top.add(left, BorderLayout.WEST);
        FontStrip fs = new FontStrip();
        top.add(fs, BorderLayout.CENTER);
        // sub-panel to hold right-side buttons
        JPanel rb = new JPanel(new BorderLayout());
        top.add(rb, BorderLayout.EAST);
        JButton right = new JButton(">");
        right.setActionCommand("right");
        right.addActionListener(this);
        rb.add(right, BorderLayout.WEST);
        JButton add = new JButton("add");
        add.setActionCommand("add");
        add.addActionListener(this);
        rb.add(add, BorderLayout.CENTER);
        JButton rem = new JButton("rem");
        rem.setActionCommand("rem");
        rem.addActionListener(this);
        rb.add(rem, BorderLayout.EAST);
        _frame.setVisible(true);
    }
    private class FontStrip extends JComponent {
        public void paintComponent(Graphics g) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void actionPerformed(ActionEvent e) {
        _log.error(e.toString());
    }

    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'x')
            _frame.dispose();
    }
}
