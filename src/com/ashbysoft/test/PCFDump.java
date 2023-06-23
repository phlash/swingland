package com.ashbysoft.test;

import com.ashbysoft.swingland.GraphicsEnvironment;

public class PCFDump {
    public static void main(String[] args) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (var f : ge.getAllFonts()) {
            System.out.println("Family: "+f.getFamilyName()+", Font: "+f.getFontName());
        }
    }
}
