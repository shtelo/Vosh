package org.iptime.shtelo.vosh.server.web;

import org.bukkit.Bukkit;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.listeners.MoveListener;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server implements Runnable {
    private MoveListener moveListener;
    private Main plugin;

    private Thread thread;

    private ServerSocket serverSocket;

    private ArrayList<ServerSideThread> threads;
    private ArrayList<ServerSideThread> threadsRemoveQueue;

    private boolean running;

    public void announce(String string) {
        refreshQueue();
        for (ServerSideThread serverSideThread : threads) {
            serverSideThread.sendWithoutLog(string);
        }
        Bukkit.getConsoleSender().sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                "&lCLIENTS&r" + " < " + string));
    }

    public void passStringToOthers(String string, ServerSideThread from) {
        refreshQueue();
        for (ServerSideThread serverSideThread : threads) {
            if (serverSideThread != from) {
                serverSideThread.sendWithoutLog(string);
            }
        }
    }

    public void passBytesToOthers(byte[] bytes, ServerSideThread from) {
        refreshQueue();
        for (ServerSideThread serverSideThread : threads) {
            if (serverSideThread != from) {
                try {
                    serverSideThread.sendBytes(bytes);
                } catch (IOException ignored) {}
            }
        }
    }

    public Server(MoveListener moveListener, Main plugin) {
        this.moveListener = moveListener;
        this.plugin = plugin;

        threads = new ArrayList<>();
        threadsRemoveQueue = new ArrayList<>();

        running = false;
    }

    public void start() throws IOException {
        thread = new Thread(this);

        serverSocket = new ServerSocket(Constants.PORT);

        thread.start();
    }

    @Override
    public void run() {
        running = true;

        Bukkit.getConsoleSender().sendMessage(
                Utils.chat(Constants.CHATTING_PREFIX + " 서버가 포트 " + Constants.PORT + "에서 시작되었습니다."));

        while (plugin.isEnabled() && running) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                ServerSideThread serverSideThread = new ServerSideThread(moveListener, socket, this, plugin);
                serverSideThread.start();
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        refreshQueue();
        for (ServerSideThread serverSideThread : threads) {
            serverSideThread.stop();
        }

        running = false;

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage(
                Utils.chat(Constants.CHATTING_PREFIX + " 서버가 종료되었습니다."));

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void refreshQueue() {
        for (ServerSideThread serverSideThread : threadsRemoveQueue) {
            threads.remove(serverSideThread);
        }
        threadsRemoveQueue.clear();
    }

    public void queueRemove(ServerSideThread serverSideThread) {
        threadsRemoveQueue.add(serverSideThread);
    }

    public ArrayList<ServerSideThread> getThreads() {
        return threads;
    }

    public boolean isRunning() {
        return running;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public boolean isName(String name) {
        refreshQueue();
        for (ServerSideThread serverSideThread : threads) {
            if (serverSideThread.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
