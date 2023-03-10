package com.ashbysoft.swingland;

public class JRootPane extends JComponent {
    // decoration styles
    public static final int NONE = 0;
    public static final int FRAME = 1;
    public static final int PLAIN_DIALOG = 2;
    public static final int INFORMATION_DIALOG = 3;
    public static final int ERROR_DIALOG = 4;
    public static final int COLOR_CHOOSER_DIALOG = 5;
    public static final int FILE_CHOOSER_DIALOG = 6;
    public static final int QUESTION_DIALOG = 7;
    public static final int WARNING_DIALOG = 8;

    private Component _glassPane;
    private JLayeredPane _layeredPane;
    private Container _contentPane;
    private JMenuBar _menuBar;
    private JButton _defaultButton;
    private int _decorationStyle;
    public JRootPane() {
        // according to the text & diagrams here:
        // https://docs.oracle.com/javase/10/docs/api/javax/swing/JRootPane.html
        // we should always have glass, layered and content panes, but can omit menu
        _log.info("<init>()");
        setLayout(createRootLayout());
        setGlassPane(createGlassPane());
        setLayeredPane(createLayeredPane());
        setContentPane(createContentPane());
    }
    public Component getGlassPane() { return _glassPane; }
    public void setGlassPane(Component gp) {
        _log.info("setGlassPane(...)");
        remove(_glassPane);
        _glassPane = gp;
        add(gp, 0);
    }
    public JLayeredPane getLayeredPane() { return _layeredPane; }
    public void setLayeredPane(JLayeredPane lp) {
        _log.info("setLayeredPane(...)");
        remove(_layeredPane);
        _layeredPane = lp;
        add(lp, -1);
    }
    public Container getContentPane() { return _contentPane; }
    public void setContentPane(Container cp) {
        _log.info("setContentPane(...)");
        _layeredPane.remove(_contentPane);
        _contentPane = cp;
        _layeredPane.add(cp, JLayeredPane.FRAME_CONTENT_LAYER);
    }
    public JMenuBar getJMenuBar() { return _menuBar; }
    public void setJMenuBar(JMenuBar mb) {
        _log.info("setJMenuBar("+mb.getName()+")");
        _menuBar = mb;
    }
    public JButton getDefaultButton() { return _defaultButton; }
    public void setDefaultButton(JButton db) {
        _log.info("setDefaultButton("+db.getName()+")");
        _defaultButton = db;
    }
    // Not implemented
    //public RootPaneUI getUI();
    //public void setUI(RootPaneUI);
    //public String getUIClassID();
    public int getWindowDecorationStyle() { return _decorationStyle; }
    public void setWindowDecorationStyle(int ds) {
        _log.info("setWindowDecorationStyle("+ds+")");
        _decorationStyle = ds;
    }
    // weird: https://docs.oracle.com/javase/10/docs/api/javax/swing/JRootPane.html
    public boolean isOptimizedDrawingEnabled() { return false; }
    public boolean isValidateRoot() { return true; }

    protected Component createGlassPane() {
        Component gp = new GlassPane();
        gp.setVisible(false);
        return gp;
    }
    protected class GlassPane extends JComponent {
        GlassPane() { _log.info("<init>()"); }
    };
    protected JLayeredPane createLayeredPane() {
        JLayeredPane lp = new JLayeredPane();
        // re-use our layout manager, it knows..
        lp.setLayout(getLayout());
        return lp;
    }
    protected Container createContentPane() { return new JPanel(); }
    protected LayoutManager createRootLayout() { return new RootLayout(); }

    protected class RootLayout extends NullLayout {
        RootLayout() { _log.info("<init>()"); }
        public void layoutContainer(Container parent) {
            _log.info("layoutContainer("+parent.getName()+")");
            Rectangle bounds = parent.getBounds();
            // are we laying out the root or layered panes?
            if (parent == _layeredPane) {
                // menu bar at the top if we have one
                int top = bounds._y;
                if (_menuBar != null) {
                    Dimension d = _menuBar.getPreferredSize();
                    top += d._h;
                    _menuBar.setBounds(bounds._x, bounds._y, bounds._w, d._h);
                }
                // content pane occupies remainder
                _contentPane.setBounds(bounds._x, top, bounds._w, bounds._h-top);
            } else {
                // size both underlying panes to match parent
                _glassPane.setBounds(bounds._x, bounds._y, bounds._w, bounds._h);
                _layeredPane.setBounds(bounds._x, bounds._y, bounds._w, bounds._h);
            }
        }
    }
}
