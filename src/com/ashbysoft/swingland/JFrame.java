// Top level swing frame.

package com.ashbysoft.swingland;

public class JFrame extends Frame implements WindowConstants, RootPaneContainer {
    private String _title;
    private GraphicsConfiguration _graphicsConfig;
    private Graphics _graphics;
    private JRootPane _rootPane;
    private TransferHandler _transferHandler;
    private int _defaultCloseOperation;
    private static boolean _useDefaultLookAndFeel;
    public JFrame() { this("", null); }
    public JFrame(String title) { this(title, null); }
    public JFrame(GraphicsConfiguration config) { this("", config); }
    public JFrame(String title, GraphicsConfiguration config) {
        _log.info("<init>("+title+")");
        this._title = title;
        this._graphicsConfig = config;
        this._transferHandler = new TransferHandler();
        _defaultCloseOperation = DO_NOTHING_ON_CLOSE;
        _useDefaultLookAndFeel = true;
        _rootPane = createRootPane();
        super.addImpl(_rootPane, null, 0);
    }
    protected JRootPane createRootPane() {
        JRootPane rp = new JRootPane();
        rp.setWindowDecorationStyle(JRootPane.FRAME);
        return rp;
    }
    // XXX:TODO
    //public AccessibleContext getAccessibleContext();

    public Container getContentPane() { return _rootPane.getContentPane(); }
    public void setContentPane(Container cp) { _rootPane.setContentPane(cp); }
    public int getDefaultCloseOperation() { return _defaultCloseOperation; }
    public void setDefaultCloseOperation(int op) { _defaultCloseOperation = op; }
    public Component getGlassPane() { return _rootPane.getGlassPane(); }
    public void setGlassPane(Component gp) { _rootPane.setGlassPane(gp); }
    public Graphics getGraphics() { return _graphics; }
    public JMenuBar getJMenuBar() { return _rootPane.getJMenuBar(); }
    public void setJMenuBar(JMenuBar mb) { _rootPane.setJMenuBar(mb); }
    public JLayeredPane getLayeredPane() { return _rootPane.getLayeredPane(); }
    public void setLayeredPane(JLayeredPane lp) { _rootPane.setLayeredPane(lp); }
    public JRootPane getRootPane() { return _rootPane; }
    public TransferHandler getTransferHandler() { return _transferHandler; }
    public void setTransferHandler(TransferHandler th) { _transferHandler = th; }
    public static boolean isDefaultLookAndFeelDecorated() { return _useDefaultLookAndFeel; }
    public static void setDefaultLookAndFeelDecorated(boolean lf) { _useDefaultLookAndFeel = lf; }
    public void setLayout(LayoutManager lm) { _rootPane.getContentPane().setLayout(lm); }
    // forward add/remove stuff to content pane
    protected void addImpl(Component c, Object s, int i) {
        getContentPane().add(c, s, i);
    }
    public void remove(int i) {
        getContentPane().remove(i);
    }
    public void remove(Component c) {
        getContentPane().remove(c);
    }
}
