package com.ashbysoft.wayland;

import com.ashbysoft.swingland.Logger;

import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Connection {
    private Logger _log = new Logger("[Connection@"+hashCode()+"]:");
    private SocketChannel _channel;
    private Selector _selector;
    private SelectionKey _selKey;

    public Connection() {
        this(null);
    }
    public Connection(String path) {
        connect(path);
    }
    private void connect(String path) {
        if (null == path) {
            // follows the Wayland socket naming guidelines
            // https://wayland-book.com/protocol-design/wire-protocol.html#transports
            if (System.getenv("WAYLAND_SOCKET") != null)
                throw new UnsupportedOperationException("Sorry: unable to use the connected file descriptor from WAYLAND_SOCKET");
            String wayland = System.getenv("WAYLAND_DISPLAY");
            if (null == wayland)
                wayland = "wayland-0";
            String xdg = System.getenv("XDG_RUNTIME_DIR");
            if (null == xdg)
                xdg = "/run";
            path = xdg + "/" + wayland;
        }
        _log.info("connect("+path+")");
        try {
            _channel = SocketChannel.open(UnixDomainSocketAddress.of(path));
            _channel.configureBlocking(false);
            _selector = Selector.open();
            _selKey = _channel.register(_selector, SelectionKey.OP_READ, null);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Unable to open connection to wayland: " + path, e);
        }
    }
    public void close() {
        _log.info("close()");
        if (_selector != null) {
            try { _selector.close(); } catch (java.io.IOException e) {}
            _selector = null;
        }
        if (_channel != null) {
            try { _channel.close(); } catch (java.io.IOException e) {}
            _channel = null;
        }
    }
    public boolean write(ByteBuffer b) {
        // ensure size is correct
        int op = b.getInt(4) & 0xffff;
        b.putInt(4, op | b.limit() << 16);
        // rewind the position and send it..
        b.rewind();
        try {
            while (b.remaining() > 0)
                _channel.write(b);
            logBuffer("Tx:", b);
            return true;
        } catch (java.io.IOException e) {
            _log.error(e.toString());
        }
        return false;
    }
    public boolean available() {
        try {
        if (_selector.selectNow() > 0)
            return true;
        } catch (java.io.IOException e) {
            _log.error(e.toString());
        }
        return false;
    }
    public ByteBuffer read() {
        // initial read goes to fixed size header buffer..
        ByteBuffer h = ByteBuffer.allocate(8);
        h.order(ByteOrder.nativeOrder());
        if (!fillBuffer(h))
            return null;
        h.rewind();
        // read out the header values
        int oid = h.getInt();
        int szop = h.getInt();
        // next read size is calculated from sizeOp
        int psz = ((szop>>16) & 0xffff);
        ByteBuffer b = ByteBuffer.allocate(psz);
        b.order(ByteOrder.nativeOrder());
        b.putInt(oid);
        b.putInt(szop);
        if (!fillBuffer(b))
            return null;
        logBuffer("Rx:", b);
        b.rewind();
        return b;
    }
    private boolean fillBuffer(ByteBuffer b) {
        while (b.remaining() > 0) {
            try {
                if (_channel.read(b) < 0)
                    throw new java.io.IOException("EOF on Wayland connection");
            } catch (java.io.IOException e) {
                _log.error(e.toString());
                return false;
            }
        }
        return true;
    }
    private void logBuffer(String p, ByteBuffer b) {
        b.rewind();
        StringBuffer sb = new StringBuffer();
        while (b.remaining() > 0)
            sb.append(String.format("%08x,", b.getInt()));
        _log.detail(p+"["+sb.toString()+"]");
    }
}