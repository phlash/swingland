package com.ashbysoft.swingland.wayland;

public class Callback implements MessageReceiver {
    public static final int EV_DONE = 0;

    private int id = Objects.register(this);
    private boolean done = false;
    public int getID() { return id; }
    public boolean handle(WaylandMessage e) {
        if (e.opcode() == EV_DONE) {
            done = true;
            return true;
        }
        return false;
    }
    public boolean done() { return done; }
}
