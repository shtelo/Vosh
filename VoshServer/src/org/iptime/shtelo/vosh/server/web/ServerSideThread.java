package org.iptime.shtelo.vosh.server.web;

import org.bukkit.Bukkit;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerSideThread implements Runnable {
    private Socket socket;
    private Server server;
    private Main plugin;

    private PrintStream printStream;
    private Scanner scanner;

    private Thread thread;

    public ServerSideThread(Socket socket, Server server, Main plugin) {
        this.socket = socket;
        this.server = server;
        this.plugin = plugin;
    }

    public void start() throws IOException {
        thread = new Thread(this);

        server.getThreads().add(this);

        printStream = new PrintStream(socket.getOutputStream());
        scanner = new Scanner(socket.getInputStream());

        thread.start();

        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                socket.getInetAddress().getHostName() + "이(가) 보스 클라이언트를 시작했습니다."));
    }

    public void stop() {
        server.queueRemove(this);

        scanner.close();
        printStream.close();

        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                socket.getInetAddress().getHostName() + "이(가) 보스 클라이언트를 종료했습니다."));

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String data;
        while (plugin.isEnabled()) {
            if (scanner.hasNextLine()) {
                try {
                    data = scanner.nextLine();
                } catch (NoSuchElementException e) {
                    break;
                }
                printStream.println(data);

                String[] args = data.split(" ");

                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("QUIT")) {
                        break;
                    }
                }
            }
        }

        stop();
    }
}
