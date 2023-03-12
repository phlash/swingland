package com.ashbysoft.wayland;

public class Test {
    public static void main(String[] args) {
        Display d = new Display();
        System.out.println("pumping..");
        while (d.dispatch())
            try { Thread.currentThread().sleep(1000); } catch (Exception e) {}
    }
}