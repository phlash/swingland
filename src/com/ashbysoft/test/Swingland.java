package com.ashbysoft.test;

import java.io.IOException;

import com.ashbysoft.swingland.*;
import com.ashbysoft.swingland.event.*;
import com.ashbysoft.swingland.image.ImageIO;

public class Swingland extends JComponent implements ActionListener, WindowListener, Runnable {
	private String _imgResource;
    private JFrame _frame;
	private JMenuBar _mbar;
	private JPopupMenu _popup;
	private JDialog _dialog;
	private JDialog _tabbed;
	private Timer _timer;
	private int _place;
	private int[] _places = { SwingConstants.TOP, SwingConstants.LEFT, SwingConstants.BOTTOM, SwingConstants.RIGHT };
	private boolean _paused;
	private Border _border;
	private Image _testcard;
	private Image _duke;
	private int _x = 0;
	private int _y = 0;
	private int _c = 0;
	private int _b = 0;

    public void run(String[] args) {
		for (String arg : args)
			if (arg.startsWith("img:"))
				_imgResource = arg.substring(4);
		SwingUtilities.invokeLater(this);
	}
	public void run() {
		try {
			if (null == _imgResource)
				_imgResource = "/images/testcard.qoi";
			if (_imgResource.startsWith("file:"))
				_testcard = ImageIO.read(_imgResource.substring(5));
			else
				_testcard = ImageIO.read(getClass().getResourceAsStream(_imgResource));
			_duke = ImageIO.read(getClass().getResourceAsStream("/swingland-duke-wayland.png"));
		} catch (IOException e) {
			_log.error(e.toString());
			for (var s : e.getStackTrace())
				_log.error("  " + s.toString());
		}
		_log.info("-->run()");
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        // Create a top level frame and put ourselves in it.
        _frame = new JFrame("Swingland lives!");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// not setting a size gives us a default resizable window (Sway tiles us)
		//_frame.setSize(800, 600);
		_frame.setBackground(new Color(64,64,64, 128));
		_frame.add(this);
		_mbar = new JMenuBar();
		_mbar.setBackground(Color.LIGHT_GRAY);
		JMenu fm = new JMenu("File");
		fm.setMnemonic(KeyEvent.VK_F);
		JMenuItem open = new JMenuItem("Open..");
		open.setMnemonic(KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		open.addActionListener(this);
		fm.add(open);
		JMenuItem close = new JMenuItem("Close");
		close.setMnemonic(KeyEvent.VK_C);
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
		close.addActionListener(this);
		close.setEnabled(false);
		fm.add(close);
		fm.addSeparator();
		JMenuItem exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		fm.add(exit);
		_mbar.add(fm);
		JMenu em = new JMenu("Edit");
		em.setMnemonic(KeyEvent.VK_E);
		em.setEnabled(false);
		JMenuItem st = new JMenuItem("Stuff");
		st.setMnemonic(KeyEvent.VK_S);
		st.addActionListener(this);
		em.add(st);
		_mbar.add(em);
		JMenu vm = new JMenu("View");
		vm.setMnemonic(KeyEvent.VK_V);
		JMenuItem dlg = new JMenuItem("Dialog");
		dlg.setMnemonic(KeyEvent.VK_D);
		dlg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK));
		dlg.setActionCommand("popup");
		dlg.addActionListener(this);
		vm.add(dlg);
		JMenuItem tab = new JMenuItem("Tabbed");
		tab.setMnemonic(KeyEvent.VK_T);
		tab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK));
		tab.setActionCommand("tabbed");
		tab.addActionListener(this);
		vm.add(tab);
		JMenu sub = new JMenu("Choose >");
		sub.setMnemonic(KeyEvent.VK_H);
		JMenuItem t1 = new JMenuItem("Type #1");
		t1.setMnemonic(KeyEvent.VK_1);
		t1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		t1.addActionListener(this);
		sub.add(t1);
		JMenuItem t2 = new JMenuItem("Type #2");
		t2.setMnemonic(KeyEvent.VK_2);
		t2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		t2.addActionListener(this);
		sub.add(t2);
		vm.add(sub);
		_mbar.add(vm);
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		JMenuItem about = new JMenuItem("About");
		about.setMnemonic(KeyEvent.VK_A);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		about.setActionCommand("about");
		about.addActionListener(this);
		help.add(about);
		_mbar.setHelpMenu(help);
		_frame.setJMenuBar(_mbar);
		_frame.setVisible(true);
		_border = new ColorBorder(10, 10, 10, 10, Color.LIGHT_GRAY);
		_timer = new Timer(3000, this);
		_timer.setActionCommand("timer");
		_timer.start();
		_log.info("<--run()");
    }

	protected void processEvent(AbstractEvent e) {
		_log.info(e.toString());
		if (e instanceof KeyEvent) {
			KeyEvent k = (KeyEvent)e;
			if (k.getID() != KeyEvent.KEY_PRESSED)
				return;
			if (k.getKeyCode() == KeyEvent.VK_ESC) {
				_log.info("disposing");
				k.consume();
				_frame.dispose();
			} else if (k.getKeyCode() == KeyEvent.VK_D) {
				_log.info("dialog");
				k.consume();
				toggleDialog();
			} else if (k.getKeyCode() == KeyEvent.VK_T) {
				_log.info("tabbed");
				k.consume();
				toggleTabbedDialog();
			} else if (k.getKeyCode() == KeyEvent.VK_B) {
				_log.info("border");
				k.consume();
				if (getBorder() != null)
					setBorder(null);
				else
					setBorder(_border);
			} else if (k.getKeyCode() == KeyEvent.VK_SPACE) {
				_log.info("[un]pause");
				k.consume();
				_paused = !_paused;
			} else if (k.getKeyCode() == KeyEvent.VK_V) {
				_log.info("splitV");
				k.consume();
				if (_dialog != null)
					((JSplitPane)_dialog.getContentPane().getComponent(1)).setOrientation(JSplitPane.VERTICAL_SPLIT);
			} else if (k.getKeyCode() == KeyEvent.VK_U) {
				k.consume();
				if (getGraphicsConfiguration().getDevice().getFullScreenWindow() != null)
					getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
				else
					getGraphicsConfiguration().getDevice().setFullScreenWindow(_frame);
			} else if (k.getKeyCode() == KeyEvent.VK_R) {
				_frame.setSize(800, 600);
			}
		} else if (e instanceof MouseWheelEvent) {
			MouseWheelEvent w = (MouseWheelEvent)e;
			w.consume();
			_c = w.getWheelRotation();
		} else if (e instanceof MouseEvent) {
			MouseEvent m = (MouseEvent)e;
			if (m.getID() == MouseEvent.MOUSE_MOVE | m.getID() == MouseEvent.MOUSE_DRAGGED) {
				m.consume();
				_x = m.getX();
				_y = m.getY();
			} else if (m.getID() == MouseEvent.MOUSE_BUTTON) {
				m.consume();
				_b = m.getButton();
				repaint();
			} else if (m.getID() == MouseEvent.MOUSE_CLICKED) {
				m.consume();
				if (m.getButton() == MouseEvent.BUTTON2)
					showPopup(m.getX(), m.getY());
			}
		}
	}

	private void toggleDialog() {
		if (_dialog != null) {
			_dialog.dispose();
			_dialog = null;
		} else {
			_dialog = new JDialog(_frame, "Test dialog");
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.addWindowListener(this);
			JLabel label = new JLabel("Label..");
			label.setForeground(Color.WHITE);
			_dialog.add(label, BorderLayout.NORTH);
			JButton button = new JButton("Press me!");
			button.setActionCommand("dialog");
			button.addActionListener(this);
			JButton nope = new JButton("Not me..");
			nope.setEnabled(false);
			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, button, nope);
			split.setBorder(new ColorBorder(2, 2, 2, 2, Color.PINK));
			split.setForeground(Color.RED);
			split.setResizeWeight(0.5);
			split.setFocus(button);
			_dialog.add(split, BorderLayout.CENTER);
			_dialog.getContentPane().setFocus(split);
			JLabel dead = new JLabel("Disabled label");
			dead.setEnabled(false);
			_dialog.add(dead, BorderLayout.SOUTH);
			_dialog.setBounds(getWidth()/2-150, getHeight()/2-100, 300, 300);
			//_dialog.pack();
			_dialog.setVisible(true);
		}
	}

	private void toggleTabbedDialog() {
		if (_tabbed != null) {
			_tabbed.dispose();
			_tabbed = null;
			return;
		}
		_tabbed = new JDialog(_frame, "Tabbed dialog..");
		_tabbed.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_tabbed.addWindowListener(this);
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Woot!", new JLabel("woot tab"));
		tab.getComponentAt(0).setBackground(Color.CYAN);
		tab.setBackgroundAt(0, Color.CYAN);
		tab.setForegroundAt(0, Color.RED);
		tab.addTab("Yeah", new JLabel("baby!"));
		tab.getComponentAt(1).setBackground(Color.MAGENTA);
		tab.setBackgroundAt(1, Color.MAGENTA);
		tab.setForegroundAt(1, Color.GREEN);
		tab.addTab("Neat", new JLabel("stuff"));
		tab.getComponentAt(2).setBackground(Color.YELLOW);
		tab.setBackgroundAt(2, Color.YELLOW);
		tab.setForegroundAt(2, Color.BLUE);
		_tabbed.add(tab);
		_tabbed.setBounds(getWidth()/3-100, getHeight()/3-100, 300, 300);
		_tabbed.setVisible(true);
		_place = 0;
	}

	private void showPopup(int x, int y) {
		if (_popup != null)
			_popup.dispose();
		_popup = new JPopupMenu(_frame, "Context!");
		_popup.add(new JMenuItem("Foogle.."));
		_popup.add(new JMenuItem("Gargle?"));
		_popup.addWindowListener(this);
		Rectangle us = getBounds();
		Component c = getParent();
		while (c != null && c != _frame) {
			us = us.offset(c.getBounds());
			c = c.getParent();
		}
		_popup.setLocation(x+us._x, y+us._y);
		_popup.setVisible(true);
	}

	public void actionPerformed(ActionEvent a) {
		_log.error("action:src="+a.getSource().toString()+",cmd="+a.getActionCommand());
		if (a.getActionCommand() == null)
			return;
		switch (a.getActionCommand()) {
			case "popup":
				toggleDialog();
				break;
			case "dialog":
				_dialog.dispose();
				_dialog = null;
				break;
			case "tabbed":
				toggleTabbedDialog();
				break;
			case "timer":
				if (_tabbed != null && !_paused) {
					_place = (_place + 1) % _places.length;
					((JTabbedPane)_tabbed.getContentPane().getComponent(0)).setTabPlacement(_places[_place]);
					_log.error("tabPlacement:"+_places[_place]);
				}
				break;
			case "exit":
				SwingUtilities.invokeLater(new Runnable() {
					public void run() { System.exit(0); }
				});
				break;
		}
	}
    public void windowOpened(WindowEvent w) {}
    public void windowClosing(WindowEvent w) {}
    public void windowClosed(WindowEvent w) {
		_log.error("windowEvent:src="+w.getSource().toString());
		// check who closed and tidy up
		if (w.getSource().equals(_dialog))
			_dialog = null;
		else if (w.getSource().equals(_tabbed))
			_tabbed = null;
		else if (w.getSource().equals((_popup)))
			_popup = null;
	}

	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), getParent().getHeight());
	}

    public void paintComponent(Graphics g) {
		_log.info("Test:paint");
		long start = System.currentTimeMillis();
		// adjust bounds for insets
		Insets ins = getInsets();
		int t = ins._t;
		int l = ins._l;
		int b = getHeight() - ins._b;
		int r = getWidth() - ins._r;
		// pinwheel at top right, with various ellipses
		g.setColor(Color.CYAN);
		int cx = (r-l)*3/4 + l;
		int cy = (b-t)/4 + t;
		int rad = (r-l) > (b-t) ? (b-t)/5 : (r-l)/5;
		for (int a = 0; a < 360; a += 30) {
			double ar = Math.toRadians(a);
			double ox = Math.cos(ar) * rad;
			double oy = Math.sin(ar) * rad;
			g.drawLine(cx, cy, cx+(int)ox, cy+(int)oy);
		}
		g.drawOval(cx-rad, cy-rad, 2*rad, 2*rad);
		g.setColor(Color.MAGENTA);
		g.drawOval(cx-rad, cy-3*rad/4, 2*rad, 3*rad/2);
		for (int a = 2; a <= 32; a *= 2)
			g.drawOval(cx-rad, cy-rad/a, 2*rad, 2*rad/a);
		g.setColor(Color.YELLOW);
		g.drawOval(cx-3*rad/4, cy-rad, 3*rad/2, 2*rad);
		for (int a = 2; a <= 32; a *= 2)
			g.drawOval(cx-rad/a, cy-rad, 2*rad/a, 2*rad);
		// nested (rounded)rects at lower right
		g.setColor(Color.YELLOW);
		cy = (b-t)*3/4 + t;
		g.drawRect(cx-rad, cy-rad, 2*rad, 2*rad);
		g.setColor(Color.RED);
		g.fillRect(cx-rad+1, cy-rad+1, 2*rad-1, 2*rad-1);
		g.setColor(Color.YELLOW);
		rad = rad*2/3;
		g.fillRoundRect(cx-rad, cy-rad, 2*rad, 2*rad, rad/4, rad/4);
		g.setColor(Color.RED);
		rad = rad*2/3;;
		g.drawRoundRect(cx-rad, cy-rad, 2*rad, 2*rad, rad/4, rad/4);
		rad = rad*2/3;;
		g.fillRoundRect(cx-rad, cy-rad, 2*rad, 2*rad, rad/4, rad/4);
		// nested circles at lower left
		g.setColor(Color.GREEN);
		cx = (r-l)/4 + l;
		rad = (r-l) > (b-t) ? (b-t)/5 : (r-l)/5;
		g.drawOval(cx-rad, cy-rad, 2*rad, 2*rad);
		g.setColor(Color.WHITE);
		g.fillOval(cx-rad+1, cy-rad+1, 2*rad-1, 2*rad-1);
		g.setColor(Color.GREEN);
		rad = rad*2/3;
		g.fillOval(cx-rad, cy-rad, 2*rad, 2*rad);
		g.setColor(Color.WHITE);
		rad = rad*2/3;
		g.drawOval(cx-rad, cy-rad, 2*rad, 2*rad);
		rad = rad*2/3;
		g.fillOval(cx-rad, cy-rad, 2*rad, 2*rad);
		g.setColor(Color.GREEN);
		g.fillOval(cx-12, cy-12, 25, 25);
		g.setColor(Color.WHITE);
		g.fillOval(cx-5, cy-5, 10, 10);
		g.setColor(Color.GREEN);
		g.fillOval(cx-3, cy-3, 6, 6);
		// instructions
		g.setColor(Color.MAGENTA);
		g.drawString("ABCDE... abcde... (ESC to quit, D for dialog, U to toggle fUllscreen, R to resize to 800x600)", l+5, b-5);
		String m = "Mouse("+_x+","+_y+","+_c+")="+_b;
		g.drawString(m, getWidth()-getFontMetrics(getFont()).stringWidth(m)-10, t+20);
		// show our current graphics config
		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle gcr = gc.getBounds();
		String gcs = "GC("+gc.getDevice().getIDstring()+"):x="+gcr._x+",y="+gcr._y+",w="+gcr._w+",h="+gcr._h;
		g.drawString(gcs, getWidth()-getFontMetrics(getFont()).stringWidth(gcs)-10, t+45);
		if (_testcard != null)
			g.drawImage(_testcard, (getWidth()-_testcard.getWidth(null))/2, (getHeight()-_testcard.getHeight(null))/2);
		if (_duke != null)
			g.drawImage(_duke, l, t);
		long dur = System.currentTimeMillis() - start;
		_log.error("render: msecs="+dur);
	}
}
