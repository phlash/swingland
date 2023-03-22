package com.ashbysoft.swingland;

import com.ashbysoft.swingland.event.*;

public class JButton extends JComponent {
    private String _text;
    public JButton() { this(""); }    
    public JButton(String text) { _text = text; }
    public void addActionListener(ActionListener l) { addEventListener(l); }
    public void removeActionListener(ActionListener l) { removeEventListener(l); }
}
