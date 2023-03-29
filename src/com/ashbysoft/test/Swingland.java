package com.ashbysoft.test;

import com.ashbysoft.swingland.*;
import com.ashbysoft.swingland.event.*;

public class Swingland extends JComponent {
    private JFrame _frame;
	private Dialog _dialog;
	private int _x = 0;
	private int _y = 0;
	private int _b = 0;

    public void run(String[] args) {
        // Create a top level frame and put ourselves in it.
        _frame = new JFrame("Swingland lives!");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setSize(800, 600);
		_frame.setBackground(new Color(64,64,64,128));
		_frame.add(this);
		setBorder(new ColorBorder(10, 10, 10, 10, Color.LIGHT_GRAY));
		_frame.setVisible(true);
    }

	public void processEvent(AbstractEvent e) {
		_log.info(e.toString());
		if (e instanceof KeyEvent) {
			KeyEvent k = (KeyEvent)e;
			if (k.getID() != KeyEvent.KEY_PRESSED)
				return;
			if (k.getKeyCode() == KeyEvent.VK_ESC) {
				_log.info("disposing");
				_frame.dispose();
			} else if (k.getKeyCode() == KeyEvent.VK_D) {
				_log.info("dialog");
				toggleDialog();
			}
		} else if (e instanceof MouseEvent) {
			MouseEvent m = (MouseEvent)e;
			if (m.getID() == MouseEvent.MOUSE_MOVE) {
				_x = m.getX();
				_y = m.getY();
			} else if (m.getID() == MouseEvent.MOUSE_BUTTON) {
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
			_dialog.setBounds(getWidth()/2-100, getHeight()/2-50, 200, 100);
			_dialog.setForeground(Color.WHITE);
			_dialog.setBackground(Color.DARK_GRAY);
			JLabel label = new JLabel("Label..");
			label.setForeground(Color.WHITE);
			_dialog.add(label);
			_dialog.setVisible(true);
		}
	}

    public void paintComponent(Graphics g) {
		_log.info("Test:paint");
		g.setColor(Color.CYAN);
		g.drawLine(1, 1, getWidth()-1, getHeight()-1);
		g.setColor(Color.YELLOW);
		g.drawLine(1, getHeight()-1, getWidth()-1, 1);
		g.setColor(Color.MAGENTA);
		g.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz (press ESC to quit, D for dialog test)", 20, getHeight()-15);
		String m = "Mouse("+_x+","+_y+")="+_b;
		g.drawString(m, getWidth()-160, 30);
	}
}