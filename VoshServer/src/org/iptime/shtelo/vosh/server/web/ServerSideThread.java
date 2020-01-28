package org.iptime.shtelo.vosh.server.web;

import org.bukkit.Bukkit;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.listeners.MoveListener;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class ServerSideThread implements Runnable {
    private MoveListener moveListener;
    private Socket socket;
    private Server server;
    private Main plugin;

    private PrintStream printStream;
    private Scanner scanner;

    private Thread thread;

    private boolean connected;

    private String name;

    public ServerSideThread(MoveListener moveListener, Socket socket, Server server, Main plugin) {
        this.moveListener = moveListener;
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
                result = result + (char) ((int) 'a' + random.nextInt(26));
            }
        } while (server.isName(result));
        return result;
    }

    @NotNull
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

    @NotNull
    private String receive() {
        String data = scanner.nextLine();
        if (!data.split(" ")[0].trim().equals(Constants.VOICE_PREFIX)) {
            Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                    getAddress() + " > " + data));
        }
        return data;
    }

    @Nullable
    private byte[] receiveBytes() {
        try {
            return socket.getInputStream().readNBytes(Constants.BUFFER_SIZE);
        } catch (IOException ignored) {}
        return null;
    }

    public void send(String string) {
        sendWithoutLog(string);
        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                getAddress() + " < " + string));
}

    public void sendBytes(byte[] data) throws IOException {
        socket.getOutputStream().write(data);
    }

    public void sendWithoutLog(String string) {
        printStream.println(string);
    }

    public String getName() {
        return name;
    }

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
                    if (args[0].trim().equals(Constants.VOICE_PREFIX)) {
                        server.passStringToOthers(data, this);
                        continue;
                    }

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
                    } else if (args[0].equalsIgnoreCase("GPOS")) {
                        // GPOS zer0ken -> position of zer0ken: GPOS zer0ken 1 2 3
                        double[] position = moveListener.getPosition(args[1]);
                        send(String.format("GPOS %s %f %f %f", args[1], position[0], position[1], position[2]));
                    } else if (args[0].equalsIgnoreCase("GYAW")) {
                        // GYAW Sch_0q0 -> y_rotation of Sch_0q0: GYAW Sch_0q0 0
                        send(String.format("GYAW %s %f", args[1], moveListener.getYaw(args[1])));
                    }
                }
            }
        }

        stop();
    }
}
