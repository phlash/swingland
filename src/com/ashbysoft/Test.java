package com.ashbysoft;

public class Test {
    public static void main(String[] args) {
        String dbg = System.getenv("WAYLAND_DEBUG");
        if (dbg != null)
            System.setProperty("ashbysoft.log.level", dbg);
        for (String arg : args) {
            if (arg.equalsIgnoreCase("wayland"))
                new com.ashbysoft.wayland.Test().run();
            else if (arg.equalsIgnoreCase("swingland"))
                new com.ashbysoft.swingland.Test().run();
            else
                System.out.println("usage: Test [wayland|swingland] [...]");
        }
    }
}