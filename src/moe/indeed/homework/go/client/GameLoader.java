package moe.indeed.homework.go.client;

import moe.indeed.homework.go.client.gui.DiscoveryFrame;

import javax.swing.*;

public class GameLoader {

    public static void main(String args[]) {
        DiscoveryFrame frame = (new DiscoveryFrame());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.discovery();
    }
}
