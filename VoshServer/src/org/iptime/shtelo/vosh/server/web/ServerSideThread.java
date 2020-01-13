package org.iptime.shtelo.vosh.server.web;

import org.bukkit.Bukkit;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class ServerSideThread implements Runnable {
    private Socket socket;
    private Server server;
    private Main plugin;

    private PrintStream printStream;
    private Scanner scanner;

    private Thread thread;

    private boolean connected;

    private String name;

    public ServerSideThread(Socket socket, Server server, Main plugin) {
        this.socket = socket;
        this.server = server;
        this.plugin = plugin;

        name = generateName();
    }

    private String generateName() {
        String result = "";
        Random random = new Random();
        do {
            for (int i = 0; i < 17; i++) {
                result = result + (char)((int)'a' + random.nextInt(26));
            }
        } while (server.isName(result));
        return result;
    }

    private String getAddress() {
        return socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    public void start() {
        thread = new Thread(this);
        server.getThreads().add(this);

        thread.start();

        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                getAddress() + "이(가) 보스 클라이언트를 시작했습니다."));
    }

    public void stop() {
        if (!connected) return;
        connected = false;

        server.queueRemove(this);

        scanner.close();
        printStream.close();

        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                getAddress() + "이(가) 보스 클라이언트를 종료했습니다."));

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String receive() {
        String data = scanner.nextLine();
        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                getAddress() + " > " + data));
        return data;
    }

    public void send(String string) {
        sendWithoutLog(string);
        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                getAddress() + " < " + string));
    }

    public void sendWithoutLog(String string) {
        printStream.println(string);
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void run() {
        if (connected) return;
        connected = true;
        try {
            printStream = new PrintStream(socket.getOutputStream());
            scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.announce("JOIN " + name);

        String data;
        boolean hasNextLine;
        while (plugin.isEnabled() && connected) {
            try {
                hasNextLine = scanner.hasNextLine();
            } catch (IllegalStateException e) {
                break;
            }
            if (hasNextLine) {
                try {
                    data = receive();
                } catch (NoSuchElementException e) {
                    break;
                }

                String[] args = data.split(" ");

                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("QUIT")) {
                        server.announce("QUIT " + name);
                        break;
                    } else if (args[0].equalsIgnoreCase("PING")) {
                        send("PONG");
                    } else if (args[0].equalsIgnoreCase("CHAT")) {
                        server.announce("CHAT " + name + " " + data.substring(5));
                    } else if (args[0].equalsIgnoreCase("NAME")) {
                        server.announce("NAME " + name + " " + args[1]);
                        name = args[1];
                    } else if (args[0].equalsIgnoreCase("QNME")) {
                        send("QNME " + name);
                    }
                }
            }
        }

        stop();
    }
}
