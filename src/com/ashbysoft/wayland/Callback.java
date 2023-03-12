package com.ashbysoft.wayland;

public class Callback extends WaylandObject {
    public static final int EV_DONE = 0;

    private boolean done = false;
    public boolean handle(WaylandMessage e) {
        if (e.opcode() == EV_DONE) {
            done = true;
            return true;
        }
        return false;
    }
    public boolean done() { return done; }
}
