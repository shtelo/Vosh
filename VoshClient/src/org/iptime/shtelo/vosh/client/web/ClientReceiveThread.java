package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

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
                    if (args[0].equals(Constants.VOICE_PREFIX)) {
                        byte[] voice = client.receiveBytes(Constants.BUFFER_SIZE);
                        client.getClientVoiceReceiveThread().offer(data, voice);
                        continue;
                    }

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
            } catch (NullPointerException ignored) {
            }
        }

        stop();
    }
}
