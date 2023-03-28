package com.ashbysoft.test;

public class Test {
    public static void main(String[] args) {
        String dbg = System.getenv("WAYLAND_DEBUG");
        if (dbg != null)
            System.setProperty("ashbysoft.log.level", dbg);
        for (String arg : args) {
            if (arg.equalsIgnoreCase("wayland"))
                new Wayland().run(args);
            else if (arg.equalsIgnoreCase("swingland"))
                new Swingland().run(args);
            else
                System.out.println("usage: Test [wayland|swingland] [...]");
        }
    }
}
