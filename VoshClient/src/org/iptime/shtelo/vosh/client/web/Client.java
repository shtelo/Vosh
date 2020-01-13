package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.forms.ChatForm;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ChatForm chatForm;

    private PrintStream printStream;
    private Scanner scanner;
    private ClientSideThread clientSideThread;

    private boolean connected;

    private String name;

    public Client(String name, Socket socket, ChatForm chatForm) throws IOException {
        this.name = name;
        this.socket = socket;
        this.chatForm = chatForm;

        connected = true;

        this.printStream = new PrintStream(socket.getOutputStream());
        this.scanner = new Scanner(socket.getInputStream());

        send("NAME " + name);

        clientSideThread = new ClientSideThread(this);
        clientSideThread.start();
    }

    public void send(String string) {
        printStream.println(string);
        chatForm.addLog("SERVER", "<-", string);
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
