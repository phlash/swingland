package com.ashbysoft.swingland.wayland;

public interface MessageReceiver {
    int getID();
    boolean handle(WaylandMessage e);
}