package com.ashbysoft.wayland;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;

public class Native {
    static {
        try {
            // Try external file (in java.libray.path) first, allows development override
            System.loadLibrary("native");
        } catch (UnsatisfiedLinkError lx) {
            // Nope? Try embedded package..
            try {
                File tmp = File.createTempFile("swg", ".lib");
                tmp.deleteOnExit();
                try (InputStream is = Native.class.getResourceAsStream("/bin/"+System.mapLibraryName("native"));
                    FileOutputStream os = new FileOutputStream(tmp)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) {
                        os.write(buf, 0, n);
                    }
                }
                System.load(tmp.getAbsolutePath());
            } catch (IOException ix) {
                ix.printStackTrace(System.err);
                System.exit(1);
            } catch (UnsatisfiedLinkError ux) {
                ux.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }
    // creates a new shared memory handle, and sets the allocated size (shm_open, ftruncate)
    public static native int createSHM(String name, int size);
    // maps an existing shared memory handle to a Java accessible buffer (mmap)
    // can be used with either a newly created handle or one transferred from the Wayland server
    public static native java.nio.ByteBuffer mapSHM(int fd, int size);
    // closes both the memory mapping and the shared memory handle (munmap, close)
    public static native void releaseSHM(int fd, java.nio.ByteBuffer buffer);
    // reads the number of bytes available on the unix socket.
    // NB: I previously tried Selector stuff, but it simply doesn't work :-(
    public static native int available(SocketChannel sock);
    // sends the file descriptor across the unix socket along with the message bytes
    public static native boolean sendFD(SocketChannel sock, byte[] msg, int fd);
}
