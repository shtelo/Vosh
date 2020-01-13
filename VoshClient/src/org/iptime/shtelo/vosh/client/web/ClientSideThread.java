package org.iptime.shtelo.vosh.client.web;

import java.util.NoSuchElementException;

public class ClientSideThread implements Runnable {
    private Client client;

    private Thread thread;

    public ClientSideThread(Client client) {
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

    @Override
    public void run() {
        String data;
        while (true) {
            try {
                data = client.receive();
            } catch (NoSuchElementException e) {
                break;
            }
        }

        stop();
    }
}
