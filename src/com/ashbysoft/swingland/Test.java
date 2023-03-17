package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class Test extends Component implements KeyListener {
    private JFrame _frame;

    public void run() {
        // Create a top level frame and put ourselves in it.
        _frame = new JFrame("Swingland lives!");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setSize(800, 600);
		_frame.add(this);
		_frame.addKeyListener(this);
		_frame.setVisible(true);
    }

	public void keyPressed(KeyEvent k) {}
	public void keyReleased(KeyEvent k) {}
	public void keyTyped(KeyEvent k) {}

    public void paint(Graphics g) {}
}