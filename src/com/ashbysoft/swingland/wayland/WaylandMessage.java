package com.ashbysoft.swingland.wayland;

import java.util.List;
import java.util.ArrayList;

public class WaylandMessage {
    private int objectID;
    private int sizeOpcode;
    private List<Integer> params;
    public WaylandMessage(int oid, int szOp) {
        objectID = oid;
        sizeOpcode = szOp;
    }
    public void add(int p) {
        if (null == params)
            params = new ArrayList<Integer>();
        params.add(p);
    }
    public int object() {
        return objectID;
    }
    public int sizeOpcode() {
        return sizeOpcode;
    }
    public int size() {
        return (sizeOpcode >> 16) & 0xffff;
    }
    public int opcode() {
        return sizeOpcode & 0xffff;
    }
    public int updateSize() {
        int nsz = ((params != null ? params.size() : 0) + 2) * 4;
        sizeOpcode = opcode() | (nsz << 16);
        return nsz;
    }
    public List<Integer> params() {
        return params;
    }
    public int param(int i) {
        return params.get(i);
    }
}
