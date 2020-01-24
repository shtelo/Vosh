package org.iptime.shtelo.vosh.client.web;

import java.util.NoSuchElementException;

public class ClientReceiveThread implements Runnable {
    private Client client;

    private Thread thread;

    public ClientReceiveThread(Client client) {
        this.client = client;

        thread = new Thread(this);
    }

    public void start() {
        thread.start();
    }

    private void stop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void run() {
        String data;
        while (client.isConnected()) {
            try {
                data = client.receive();

                String[] args = data.split(" ");

                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("PING")) {
                        client.send("PONG");
//                    } else if (args[0].equalsIgnoreCase("QUIT")) {
//
//                    } else if (args[0].equalsIgnoreCase("NAME")) {
//
                    } else if (args[0].equalsIgnoreCase("QNME")) {
                        client.setName(args[1]);
                    }
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }

        stop();
    }
}
