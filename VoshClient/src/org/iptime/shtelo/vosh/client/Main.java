package org.iptime.shtelo.vosh.client;

import org.iptime.shtelo.vosh.client.forms.LoginForm;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        new LoginForm().start();
    }
}
