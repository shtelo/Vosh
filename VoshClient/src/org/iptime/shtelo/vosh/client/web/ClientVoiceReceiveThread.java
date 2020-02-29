package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientVoiceReceiveThread implements Runnable {
    private Client client;

    private SourceDataLine line;

    private Thread thread;

    private LinkedBlockingDeque<String> voiceQueue;

    public ClientVoiceReceiveThread(Client client) {
        this.client = client;

        voiceQueue = new LinkedBlockingDeque<>();
    }

    public void offer(String data) {
        voiceQueue.offer(data);
    }

    public void start() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(
                Constants.SAMPLE_RATE, 16, 1, true, true);
        line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat, Constants.SAMPLE_RATE);
        line.start();

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
        String[] rawData;
        byte[] voice;
        Base64.Decoder decoder = Base64.getDecoder();

        while (client.isConnected()) {
            try {
                rawData = Objects.requireNonNull(voiceQueue.poll()).trim().split(" ", 3);
                voice = decoder.decode(rawData[2]);

                String playerName = rawData[1];
                double[] playerPosition = client.getPositions().get(playerName);
                double[] myPosition = client.getPositions().get(client.getName());
                double theta = client.getYaw() * (Math.PI / 180);

                System.out.println(theta + " " + Arrays.toString(myPosition) + " " + Arrays.toString(playerPosition));

                line.write(voice, 0, voice.length);
            } catch (NullPointerException ignored) {
            }
        }
        stop();
    }
}
