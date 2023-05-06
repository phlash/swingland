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
import com.ashbysoft.swingland.ColorBorder;
import com.ashbysoft.swingland.Cursor;
import com.ashbysoft.swingland.Dimension;
import com.ashbysoft.swingland.Font;
import com.ashbysoft.swingland.FontMetrics;
import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.JButton;
import com.ashbysoft.swingland.JComponent;
import com.ashbysoft.swingland.JFrame;
import com.ashbysoft.swingland.JLabel;
import com.ashbysoft.swingland.JPanel;
import com.ashbysoft.swingland.SwingUtilities;
import com.ashbysoft.swingland.event.ActionEvent;
import com.ashbysoft.swingland.event.ActionListener;
import com.ashbysoft.swingland.event.KeyEvent;
import com.ashbysoft.swingland.event.KeyListener;
import com.ashbysoft.swingland.event.MouseEvent;
import com.ashbysoft.swingland.event.MouseInputListener;

public class Fed implements ActionListener, KeyListener, Runnable {
    public static void main(String[] args) {
        System.out.println("--- (Ashbysoft *) Swingland font editor ---");
        new Fed().boot(args);
    }

    private Logger _log = new Logger("[Fed]:");
    private EditableFont _font;
    private String _cursor;
    private JFrame _frame;
    private JLabel _status;

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
            else if (args[a].startsWith("-w"))
                width = parseInt(args[++a], width);
            else if (args[a].startsWith("-h"))
                height = parseInt(args[++a], height);
            else if (args[a].startsWith("-b"))
                baseline = parseInt(args[++a], baseline);
            else if (args[a].startsWith("-l"))
                leading = parseInt(args[++a], leading);
            else if (args[a].startsWith("-o"))
                offset = parseInt(args[++a], offset);
            else if (args[a].startsWith("-m"))
                missing = parseInt(args[++a], missing);
            else if (args[a].startsWith("-c"))
                _cursor = args[++a];
        }
        if (null == name) {
            System.out.println("usage: fed -f <font file> [-w <glyph width:8>] [-h <glyph height:16>] [-b <glyph baseline:3>] [-l <glyph leading:3>]");
            System.out.println("[-o <unicode offset:0>] [-m <missing glyph:0>] [-c <use cursor:DEFAULT>]");
            return;
        }
        _font = new EditableFont(_log, new File(name), width, height, baseline, leading, offset, missing);
        SwingUtilities.invokeLater(this);
    }
    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
    public void run() {
        // adjust the cursor theme
        if (_cursor != null)
            Cursor.setTheme(_cursor);
        // build the UI
        FontMetrics fm = _font.getFontMetrics();
        String title = "Fed: "+_font.getFontName()+" ("+fm.charWidth(_font.getMissingGlyphCode())+"x"+fm.getHeight()+")";
        _frame = new JFrame(title);
        // fix frame size, set border layout for content pane
        _frame.setSize(800, 800);
        _frame.setLayout(new BorderLayout());
        // garish colours are great...
        _frame.setBackground(Color.DARK_GRAY);
        _frame.setForeground(Color.MAGENTA);
        _frame.addKeyListener(this);
        // panel to hold top row of controls
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new ColorBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        _frame.add(top, BorderLayout.NORTH);
        // controls in the top panel
        JButton left = new JButton("<");
        left.setActionCommand("left");
        left.addActionListener(this);
        top.add(left, BorderLayout.WEST);
        FontStrip fs = new FontStrip();
        fs.setForeground(Color.CYAN);
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
        // pixel editor in center of frame
        FontPixels fp = new FontPixels();
        fp.setForeground(Color.CYAN);
        _frame.add(fp, BorderLayout.CENTER);
        // status bar along bottom
        _status = new JLabel(getStatus());
        _status.setBorder(new ColorBorder(1, 1, 1, 1, Color.WHITE));
        _frame.add(_status, BorderLayout.SOUTH);
        _frame.setVisible(true);
    }
    private String getStatus() {
        FontMetrics fm = _font.getFontMetrics();
        return "  Glyph: "+(_font.getCurrent()+1)+"/"+_font.getCount()+" | codepoint: 0x"+Integer.toHexString(_font.getCurrent() + _font.getOffset())+
            " | offset: "+Integer.toHexString(_font.getOffset())+" | baseline: "+fm.getDescent()+" | leading:"+fm.getLeading();
    }
    private class FontStrip extends JComponent {
        private int _start = 0;
        public Dimension getMinimumSize() {
            FontMetrics fm = _font.getFontMetrics();
            return new Dimension(fm.charWidth(_font.getMissingGlyphCode()), fm.getHeight()+6);
        }
        public void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
            Font save = g.getFont();
            g.setFont(_font);
            FontMetrics fm = _font.getFontMetrics();
            int oy = (getHeight() - fm.getHeight()) / 2;
            // adjust start glyph if current is offscreen
            if (_font.getCurrent() < _start)
                _start = _font.getCurrent();
            while (true) {
                char [] c = new char[_font.getCurrent() - _start + 1];
                for (int p = 0; p < c.length; p += 1)
                    c[p] = (char)(_font.getOffset() + _start + p);
                int sw = fm.stringWidth(new String(c, 0, c.length));
                if (sw > getWidth())
                    _start += 1;
                else
                    break;
            }
            int cx = 0;
            for (int i = _start; i < _font.getCount(); i+= 1) {
                int cp = i + _font.getOffset();
                int cw = fm.charWidth(cp);
                char[] c = { (char)cp };
                if (_font.getCurrent() == i) {
                    g.fillRect(cx, 0, cw, getHeight());
                    g.setColor(getBackground());
                }
                g.drawChars(c, 0, 1, cx, getHeight() - oy);
                if (_font.getCurrent() == i)
                    g.setColor(getForeground());
                cx += fm.charWidth(i + _font.getOffset());
                int nw = i < _font.getCount()-1 ? fm.charWidth(cp + 1) : 0;
                if (cx + nw > getWidth())
                    break;
            }
            g.setFont(save);
        }
    }
    private class FontPixels extends JComponent implements MouseInputListener {
        private int _px;
        private int _py;
        private int _pxo;
        private int _pyo;
        private int _ppx;
        private int _ppy;
        public FontPixels() {
            addMouseInputListener(this);
        }
        public void paintComponent(Graphics g) {
            FontMetrics fm = _font.getFontMetrics();
            _px = fm.charWidth(_font.getCurrent() + _font.getOffset());
            _py = fm.getHeight();
            _ppx = getWidth() / _px;
            _ppy = getHeight() / _py;
            _pxo = 0;
            _pyo = 0;
            // adjust back to square pixels
            if (_ppx > _ppy) {
                _pxo = (getWidth() - (_px * _ppy)) / 2;
                _ppx = _ppy;
            } else if (_ppy > _ppx) {
                _pyo = (getHeight() - (_py * _ppx)) / 2;
                _ppy = _ppx;
            }
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
            for (int y = 0; y < _py; y += 1) {
                for (int x = 0; x < _px; x += 1) {
                    g.drawRect(_pxo + x * _ppx, _pyo + y * _ppy, _ppx, _ppy);
                    if (_font.getGlyphPixel(x, y))
                        g.fillRect(_pxo + x * _ppx, _pyo + y * _ppy, _ppx, _ppy);
                }
            }
        }
        public void mouseClicked(MouseEvent e) {
            // hit test..
            int x = (e.getX() - _pxo) / _ppx;
            int y = (e.getY() - _pyo) / _ppy;
            if (x >= 0 && x < _px && y >= 0 && y < _py) {
                _font.setGlyphPixel(x, y, e.getButton() == MouseEvent.BUTTON1);
                _frame.repaint();
            }
        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseDragged(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {}
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("left")) {
            _font.setCurrent(_font.getCurrent() - 1);
        } else if (e.getActionCommand().equals("right")) {
            _font.setCurrent(_font.getCurrent() + 1);
        } else if (e.getActionCommand().equals("pageleft")) {
            _font.setCurrent(_font.getCurrent() - 10);
        } else if (e.getActionCommand().equals("pageright")) {
            _font.setCurrent(_font.getCurrent() + 10);
        } else if (e.getActionCommand().equals("add")) {
            _font.addGlyph(_font.getCurrent());
        } else if (e.getActionCommand().equals("append")) {
            _font.addGlyph(_font.getCount());
        } else if (e.getActionCommand().equals("rem")) {
            _font.remGlyph(_font.getCurrent());
        }
        _status.setText(getStatus());
        _frame.repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESC)
            _frame.dispose();
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "left"));
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "right"));
        else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP)
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "pageleft"));
        else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "pageright"));
        else if (e.getKeyCode() == KeyEvent.VK_INSERT)
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "add"));
        else if (e.getKeyCode() == KeyEvent.VK_DELETE)
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "rem"));
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '+')
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRED, "append"));
    }
}
