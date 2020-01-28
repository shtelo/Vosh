package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Base64;

public class ClientVoiceSendThread implements Runnable {
    private Client client;

    private TargetDataLine targetDataLine;
    private AudioInputStream audioInputStream;

    private boolean microphoneAvailable;

    private Thread thread;

    public ClientVoiceSendThread(Client client) {
        this.client = client;

        microphoneAvailable = true;
    }

    public void start() throws LineUnavailableException {
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(
                    TargetDataLine.class,
                    new AudioFormat(Constants.SAMPLE_RATE, 16, 1, true, true)));
            targetDataLine.open();
            targetDataLine.start();
            audioInputStream = new AudioInputStream(targetDataLine);
        } catch (IllegalArgumentException e) {
            microphoneAvailable = false;
        }

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        try {
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        targetDataLine.close();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] data;
        String rawData;
        Base64.Encoder encoder = Base64.getEncoder();
        while (client.isConnected()) {
            try {
                if (microphoneAvailable) {
                    data = audioInputStream.readNBytes(Constants.BUFFER_SIZE);

                    // TODO: here comes the condition to decide to send voice to server or not (by volume maybe)
                    rawData = Constants.VOICE_PREFIX + " zer0ken " + encoder.encodeToString(data);
                    client.send(rawData);
                }

            } catch (IOException ignored) {}
        }
        stop();
    }
}
