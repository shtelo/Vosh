package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.*;
import java.beans.Encoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ClientVoiceSendThread implements Runnable {
    private Client client;

    private TargetDataLine targetDataLine;
    private AudioInputStream audioInputStream;

    private Thread thread;

    public ClientVoiceSendThread(Client client) {
        this.client = client;
    }

    public void start() throws LineUnavailableException {
        targetDataLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(
                TargetDataLine.class,
                new AudioFormat(Constants.SAMPLE_RATE, 16, 1, true, true)));
        targetDataLine.open();
        targetDataLine.start();
        audioInputStream = new AudioInputStream(targetDataLine);

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
        Base64.Encoder encoder = Base64.getEncoder();
        while (client.isConnected()) {
            try {
                data = audioInputStream.readNBytes(Constants.BUFFER_SIZE);

                // TODO: here comes the condition to decide to send voice to server or not (by volume maybe)

                client.send(Constants.VOICE_PREFIX + " 0 0 0 " + encoder.encodeToString(data));
            } catch (IOException ignored) {
            }
        }
        stop();
    }
}
