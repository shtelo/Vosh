package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import java.util.Arrays;
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
                    if (args[0].trim().equals(Constants.VOICE_PREFIX)) {
                        client.getClientVoiceReceiveThread().offer(data);
                        continue;
                    }

                    if (args[0].equalsIgnoreCase("PING")) {
                        client.send("PONG");
//                    } else if (args[0].equalsIgnoreCase("QUIT")) {

//                    } else if (args[0].equalsIgnoreCase("NAME")) {

                    } else if (args[0].equalsIgnoreCase("GPOS")) {
                        client.getPositions().put(args[1], new double[]{
                                Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])});
                    } else if (args[0].equalsIgnoreCase("GYAW")) {
                        if (args[1].equals(client.getName())) {
                            client.setYaw(Double.parseDouble(args[2]));
                        }
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
