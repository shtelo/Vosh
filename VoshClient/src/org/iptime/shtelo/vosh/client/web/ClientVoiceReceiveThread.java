package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.LinkedList;

public class ClientVoiceReceiveThread implements Runnable {
    private Client client;

    private AudioFormat audioFormat;
    private SourceDataLine line;

    private Thread thread;

    private LinkedList<byte[]> voiceQueue;
    private LinkedList<String> positionQueue;

    public ClientVoiceReceiveThread(Client client) {
        this.client = client;
        voiceQueue = new LinkedList<>();
        positionQueue = new LinkedList<>();
    }

    public void offer(String position, byte[] voice) {
        positionQueue.offer(position);
        voiceQueue.offer(voice);
    }

    public void start() throws LineUnavailableException {
        audioFormat = new AudioFormat(
                Constants.SAMPLE_RATE, 16, 1, true, true);
        line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat, Constants.SAMPLE_RATE);

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        line.close();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String[] rawPosition;
        byte[] voice;
        float[] position = new float[3];

        while (client.isConnected()) {
            try {
                rawPosition = positionQueue.poll().trim().split(" ");
                voice = voiceQueue.poll();
                for (int i = 0; i < 3; i++) {
                    position[i] = Float.parseFloat(rawPosition[i]);
                }

                // TODO: here comes the codes to control the volume of the voice

                line.write(voice, 0, voice.length);
            } catch (NullPointerException ignored) {
            }
        }
        stop();
    }
}
