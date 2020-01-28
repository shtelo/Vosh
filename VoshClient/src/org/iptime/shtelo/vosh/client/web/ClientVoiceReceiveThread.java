package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.forms.ChatForm;
import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientVoiceReceiveThread implements Runnable {
    private ChatForm chatForm;
    private Client client;

    private SourceDataLine line;

    private Thread thread;

    private LinkedBlockingDeque<String> voiceQueue;

    public ClientVoiceReceiveThread(ChatForm chatForm, Client client) {
        this.chatForm = chatForm;
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
        float[] position = new float[3];
        Base64.Decoder decoder = Base64.getDecoder();

        while (client.isConnected()) {
            try {
                rawData = voiceQueue.poll().trim().split(" ", 5);
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
