package com.ashbysoft.wayland;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Keyboard extends WaylandObject<Keyboard.Listener> {
    public interface Listener {
        boolean keymap(int format, int fd, int size);
        boolean keyboardEnter(int serial, int surface, int[] keys);
        boolean keyboardLeave(int serial, int surface);
        boolean key(int serial, int time, int keyCode, int state);
        boolean modifiers(int serial, int depressed, int latched, int locked, int group);
        boolean repeat(int rate, int delay);
    }
    public static final int RQ_RELEASE = 0;
    public static final int EV_KEYMAP = 0;
    public static final int EV_ENTER = 1;
    public static final int EV_LEAVE = 2;
    public static final int EV_KEY = 3;
    public static final int EV_MODIFIERS = 4;
    public static final int EV_REPEAT_INFO = 5;

    public Keyboard(Display d) { super(d); }
    public boolean handle(int oid, int op, int size, ByteBuffer b) {
        boolean rv = true;
        if (EV_KEYMAP == op) {
            int f = b.getInt();
            int s = b.getInt();
            // XXX:TODO - recover FD from connection..
            log(true, "keymap:format="+f+" size="+s+" no fd yet :(");
            for (Listener l : listeners())
                if (!l.keymap(f, -1, s))
                    rv = false;
        } else if (EV_ENTER == op) {
            int serial = b.getInt();
            int surface = b.getInt();
            ByteBuffer ib = ByteBuffer.wrap(getArray(b));
            ib.order(ByteOrder.nativeOrder());
            int n = ib.limit()/4;
            int[] keys = new int[n];
            for (int i=0; i<n; i++)
                keys[i] = ib.getInt();
            log(true, "enter:serial="+serial+" surface="+surface+" states="+java.util.Arrays.toString(keys));
            for (Listener l : listeners())
                if (!l.keyboardEnter(serial, surface, keys))
                    rv = false;
        } else if (EV_LEAVE == op) {
            int serial = b.getInt();
            int surface = b.getInt();
            log(true, "leave:serial="+serial+" surface="+surface);
            for (Listener l : listeners())
                if (!l.keyboardLeave(serial, surface))
                    rv = false;
        } else if (EV_KEY == op) {
            int serial = b.getInt();
            int time = b.getInt();
            int code = b.getInt();
            int state = b.getInt();
            log(true, "key:serial="+serial+" time="+time+" code="+code+" state="+state);
            for (Listener l : listeners())
                if (!l.key(serial, time, code, state))
                    rv = false;
        } else if (EV_MODIFIERS == op) {
            int serial = b.getInt();
            int depressed = b.getInt();
            int latched = b.getInt();
            int locked = b.getInt();
            int group = b.getInt();
            log(true, "modifiers:serial="+serial+" depressed="+depressed+" latched="+latched+" locked="+locked+" group="+group);
            for (Listener l : listeners())
                if (!l.modifiers(serial, depressed, latched, locked, group))
                    rv = false;
        } else if (EV_REPEAT_INFO == op) {
            int rate = b.getInt();
            int delay = b.getInt();
            log(true, "repeat:rate="+rate+" delay="+delay);
            for (Listener l : listeners())
                if (!l.repeat(rate, delay))
                    rv = false;
        } else {
            rv = unknownOpcode(op);
        }
        return rv;
    }

    public boolean release() {
        ByteBuffer b = newBuffer(8, RQ_RELEASE);
        log(false, "release");
        return _display.write(b);
    }
}
