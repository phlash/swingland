package com.ashbysoft.test;

import java.io.IOException;

import com.ashbysoft.swingland.*;
import com.ashbysoft.swingland.event.*;
import com.ashbysoft.swingland.image.ImageIO;

public class Swingland extends JComponent implements ActionListener, Runnable {
	private String _imgResource;
    private JFrame _frame;
	private Dialog _dialog;
	private Border _border;
	private Image _testcard;
	private Image _duke;
	private int _x = 0;
	private int _y = 0;
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
		_frame.setVisible(true);
		_border = new ColorBorder(10, 10, 10, 10, Color.LIGHT_GRAY);
		_log.info("<--run()");
    }

	public void processEvent(AbstractEvent e) {
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
			} else if (k.getKeyCode() == KeyEvent.VK_B) {
				_log.info("border");
				k.consume();
				if (getBorder() != null)
					setBorder(null);
				else
					setBorder(_border);
			} else if (k.getKeyCode() == KeyEvent.VK_R) {
				k.consume();
				if (_dialog != null)
					_dialog.setLocation(0, 0);
			}
		} else if (e instanceof MouseEvent) {
			MouseEvent m = (MouseEvent)e;
			if (m.getID() == MouseEvent.MOUSE_MOVE) {
				m.consume();
				_x = m.getX();
				_y = m.getY();
			} else if (m.getID() == MouseEvent.MOUSE_BUTTON) {
				m.consume();
				_b = m.getButton();
				repaint();
			}
		}
	}

	private void toggleDialog() {
		if (_dialog != null) {
			_dialog.dispose();
			_dialog = null;
		} else {
			_dialog = new Dialog(_frame, "Test dialog");
			_dialog.setBounds(getWidth()/2-150, getHeight()/2-100, 300, 200);
			_dialog.setForeground(Color.WHITE);
			_dialog.setBackground(Color.DARK_GRAY);
			JLabel label = new JLabel("Label..");
			label.setForeground(Color.WHITE);
			_dialog.add(label, BorderLayout.NORTH);
			JButton button = new JButton("Press me!");
			button.setForeground(Color.WHITE);
			button.setBackground(Color.GRAY);
			button.addActionListener(this);
			_dialog.add(button, BorderLayout.CENTER);
			_dialog.setFocus(button);
			_dialog.setVisible(true);
		}
	}

	public void actionPerformed(ActionEvent a) {
		_log.info("action!");
		_dialog.dispose();
		_dialog = null;
	}

	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), getParent().getHeight());
	}

    public void paintComponent(Graphics g) {
		_log.info("Test:paint");
		g.setColor(Color.CYAN);
		g.drawLine(1, 1, getWidth()-1, getHeight()-1);
		g.setColor(Color.YELLOW);
		g.drawLine(1, getHeight()-1, getWidth()-1, 1);
		g.setColor(Color.MAGENTA);
		g.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz (press ESC to quit, D for dialog test, R to reposition)", 20, getHeight()-15);
		String m = "Mouse("+_x+","+_y+")="+_b;
		g.drawString(m, getWidth()-160, 30);
		if (_testcard != null)
			g.drawImage(_testcard, (getWidth()-_testcard.getWidth(null))/2, (getHeight()-_testcard.getHeight(null))/2);
		if (_duke != null) {
			g.drawImage(_duke, 0, 0);
			g.drawImage(_duke, getWidth()-_duke.getWidth(null), getHeight()-_duke.getHeight(null));
		}
	}
}
