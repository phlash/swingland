package com.ashbysoft.swingland.wayland;

public class Native {
    public native int openSHM(String name);
    public native java.nio.ByteBuffer mapSHM(int fd, int size);
    public native void releaseSHM(int fd, java.nio.ByteBuffer buffer, int size);
}