package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.forms.ChatForm;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String HOST;
    private final int PORT;
    private Socket socket;
    private ChatForm chatForm;

    private PrintStream printStream;
    private Scanner scanner;

    private ClientReceiveThread clientReceiveThread;
    private ClientVoiceThread clientVoiceThread;

    private boolean connected;

    private String name;

    public Client(String name, Socket socket, String host, int port, ChatForm chatForm)
            throws IOException, LineUnavailableException {
        this.name = name;
        this.socket = socket;
        this.HOST = host;
        this.PORT = port;
        this.chatForm = chatForm;

        connected = true;

        printStream = new PrintStream(socket.getOutputStream());
        scanner = new Scanner(socket.getInputStream());

        send("NAME " + name);

        clientReceiveThread = new ClientReceiveThread(this);
        clientReceiveThread.start();
        clientVoiceThread = new ClientVoiceThread(this);
        clientVoiceThread.start();
    }

    public int getPORT() {
        return PORT;
    }

    public String getHOST() {
        return HOST;
    }

    public void quit() {
        send("QUIT");
        setConnected(false);
    }

    public void send(String string) {
        printStream.println(string);
        chatForm.addLog("SERVER", "<-", string);
    }

    public void sendByte(byte[] data) throws IOException {
        socket.getOutputStream().write(data);
    }

    public String receive() {
        if (scanner.hasNextLine()) {
            String data = scanner.nextLine();
            chatForm.addLog("SERVER", "->", data);
            return data;
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        chatForm.getUsernameLabel().setText(name);
        this.name = name;
    }
}
