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

    public Client(Socket socket, ChatForm chatForm) throws IOException {
        this.socket = socket;
        this.chatForm = chatForm;

        this.printStream = new PrintStream(socket.getOutputStream());
        this.scanner = new Scanner(socket.getInputStream());

        clientSideThread = new ClientSideThread(this);
        clientSideThread.start();
    }

    public void send(String string) {
        printStream.println(string);
        chatForm.addLog("SERVER", "<-", string);
    }

    public String receive() {
        String data = scanner.nextLine();
        chatForm.addLog("SERVER", "->", data);
        return data;
    }

}
