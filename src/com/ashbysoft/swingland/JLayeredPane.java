package com.ashbysoft.swingland;

public class JLayeredPane extends JComponent {
    public static final Integer FRAME_CONTENT_LAYER = -30000;
    public static final Integer DEFAULT_LAYER = 0;
    public static final Integer PALETTE_LAYER = 100;
    public static final Integer MODAL_LAYER = 200;
    public static final Integer POPUP_LAYER = 300;
    public static final Integer DRAG_LAYER = 400;
    public static final String LAYER_PROPERTY = "layeredContainerLayer";

    public JLayeredPane() {
    }
}