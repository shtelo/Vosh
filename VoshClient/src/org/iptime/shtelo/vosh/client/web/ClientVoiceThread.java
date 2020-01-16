package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.*;
import java.io.IOException;

public class ClientVoiceThread implements Runnable {
    private Client client;

    private AudioFormat audioFormat;
    private DataLine.Info info;
    private TargetDataLine targetDataLine;
    private AudioInputStream audioInputStream;

    private Thread thread;

    public ClientVoiceThread(Client client) {
        this.client = client;
    }

    public void start() throws LineUnavailableException {
        audioFormat = new AudioFormat(Constants.SAMPLE_RATE, 16, 1, true, true);
        info = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(); targetDataLine.start();
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
        while (client.isConnected()) {
            try {
                data = audioInputStream.readNBytes(Constants.BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stop();
    }
}
