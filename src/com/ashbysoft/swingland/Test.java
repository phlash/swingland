package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class Test extends Component implements KeyListener, MouseInputListener {
    private JFrame _frame;
	private int _x = 0;
	private int _y = 0;
	private int _b = 0;

    public void run() {
        // Create a top level frame and put ourselves in it.
        _frame = new JFrame("Swingland lives!");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setSize(800, 600);
		_frame.add(this);
		_frame.addKeyListener(this);
		_frame.addMouseInputListener(this);
		_frame.setVisible(true);
    }

	public void keyPressed(KeyEvent k) {
		if (k.getKeyCode() == KeyEvent.VK_ESC) {
			_log.info("disposing");
			_frame.dispose();
		}
	}
	public void keyReleased(KeyEvent k) {}
	public void keyTyped(KeyEvent k) { _log.info("typed:"+k.getKeyChar()); }

	public void mouseDragged(MouseEvent m) {}
	public void mouseMoved(MouseEvent m) { _x = m.getX(); _y = m.getY(); }
	public void mouseClicked(MouseEvent m) {}
	public void mouseEntered(MouseEvent m) {}
	public void mouseExited(MouseEvent m) {}
	public void mouseReleased(MouseEvent m) { _b = 0; repaint(); }
	public void mousePressed(MouseEvent m) { _b = m.getButton(); repaint(); }

    public void paint(Graphics g) {
		_log.info("Test:paint");
		g.setColor(getForeground());
		g.drawRect(1, 1, getWidth()-2, getHeight()-2);
		g.setColor(Color.CYAN);
		g.drawLine(1, 1, getWidth()-2, getHeight()-2);
		g.setColor(Color.YELLOW);
		g.drawLine(1, getHeight()-2, getWidth()-2, 1);
		g.setColor(Color.RED);
		g.drawString("Swingland rocks! (press ESC to quit)", 10, 20);
		String m = "Mouse("+_x+","+_y+")="+_b;
		g.drawString(m, getWidth()-160, 20);
	}
}