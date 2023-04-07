// Top level swing frame.

package com.ashbysoft.swingland;

public class JFrame extends Frame implements WindowConstants, RootPaneContainer {
    private JRootPane _rootPane;
    private TransferHandler _transferHandler;
    private int _defaultCloseOperation;
    public JFrame() { this(""); }
    public JFrame(String title) {
        super(title);
        _log.info("JFrame:<init>("+title+")");
        this._transferHandler = new TransferHandler();
        _defaultCloseOperation = DO_NOTHING_ON_CLOSE;
        _rootPane = createRootPane();
        super.addImpl(_rootPane, null, 0);
    }
    protected JRootPane createRootPane() {
        JRootPane rp = new JRootPane();
        rp.setWindowDecorationStyle(JRootPane.FRAME);
        return rp;
    }

    public Container getContentPane() { return _rootPane.getContentPane(); }
    public void setContentPane(Container cp) { _rootPane.setContentPane(cp); }
    public int getDefaultCloseOperation() { return _defaultCloseOperation; }
    public void setDefaultCloseOperation(int op) { _defaultCloseOperation = op; }
    public Component getGlassPane() { return _rootPane.getGlassPane(); }
    public void setGlassPane(Component gp) { _rootPane.setGlassPane(gp); }
    public JMenuBar getJMenuBar() { return _rootPane.getJMenuBar(); }
    public void setJMenuBar(JMenuBar mb) { _rootPane.setJMenuBar(mb); }
    public JLayeredPane getLayeredPane() { return _rootPane.getLayeredPane(); }
    public void setLayeredPane(JLayeredPane lp) { _rootPane.setLayeredPane(lp); }
    public JRootPane getRootPane() { return _rootPane; }
    public TransferHandler getTransferHandler() { return _transferHandler; }
    public void setTransferHandler(TransferHandler th) { _transferHandler = th; }
    // forward add/remove/layout stuff to content pane
    public void setLayout(LayoutManager lm) {
        getContentPane().setLayout(lm);
    }
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
