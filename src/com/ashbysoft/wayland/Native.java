package com.ashbysoft.wayland;

public class Native {
    // creates a new shared memory handle, and sets the allocated size (shm_open, ftruncate)
    public native int createSHM(String name, int size);
    // maps an existing shared memory handle to a Java accessible buffer (mmap)
    // can be used with either a newly created handle or one transferred from the Wayland server
    public native java.nio.ByteBuffer mapSHM(int fd, int size);
    // closes both the memory mapping and the shared memory handle (munmap, close)
    public native void releaseSHM(int fd, java.nio.ByteBuffer buffer);
}
