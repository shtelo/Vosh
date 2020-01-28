package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;

public class ClientVoiceReceiveThread implements Runnable {
    private Client client;

    private SourceDataLine line;

    private Thread thread;

    private LinkedList<String> voiceQueue;

    public ClientVoiceReceiveThread(Client client) {
        this.client = client;
        voiceQueue = new LinkedList<>();
    }

    public void offer(String data) {
        voiceQueue.offer(data);
    }

    public void start() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(
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
        String[] rawData;
        byte[] voice;
        float[] position = new float[3];
        Base64.Decoder decoder = Base64.getDecoder();

        while (client.isConnected()) {
            try {
                rawData = voiceQueue.poll().trim().split(" ", 4);
                for (int i = 0; i < 3; i++) {
                    position[i] = Float.parseFloat(rawData[i + 1]);
                }
                voice = decoder.decode(rawData[4]);

                // TODO: here comes the codes to control the volume of the voice

                line.write(voice, 0, voice.length);
            } catch (NullPointerException ignored) {
            }
        }
        stop();
    }
}
