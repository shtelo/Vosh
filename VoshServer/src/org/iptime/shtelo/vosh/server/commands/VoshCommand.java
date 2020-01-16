package org.iptime.shtelo.vosh.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;
import org.iptime.shtelo.vosh.server.web.Server;
import org.iptime.shtelo.vosh.server.web.ServerSideThread;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VoshCommand implements CommandExecutor {
    private Server server;

    private Map<String, String> argsDescription;

    public VoshCommand(Server server, Main plugin) {
        this.server = server;

        Objects.requireNonNull(plugin.getCommand("vosh")).setExecutor(this);

        argsDescription = new HashMap<>();
        argsDescription.put("stop", "보스 서버를 종료합니다.");
        argsDescription.put("start", "보스 서버를 시작합니다.");
        argsDescription.put("status", "보스 서버의 상태를 확인합니다.");
        argsDescription.put("help", "/vosh 명령어 사용법을 확인합니다.");
        argsDescription.put("list", "보스 서버에 접속중인 클라이언트 목록을 확인합니다.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (sender.hasPermission("stop")) {
                    if (server.isRunning()) {
                        server.stop();
                        sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버를 종료했습니다."));
                        return false;
                    } else {
                        sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버가 이미 꺼져 있습니다!"));
                        return true;
                    }
                } else {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 이 명령어를 사용할 권한이 없어요!"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("start")) {
                if (sender.hasPermission("start")) {
                    if (server.isRunning()) {
                        sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버가 이미 열려 있습니다!"));
                        return true;
                    } else {
                        try {
                            server.start();
                            sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버를 시작했습니다."));
                            return false;
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " +
                                    "서버를 여는 중 오류가 발생했습니다!"));
                            return true;
                        }
                    }
                } else {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 이 명령어를 사용할 권한이 없어요!"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " &eVosh 서버 상태"));
                if (server.isRunning()) {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 상태: &a켜져 있음"));
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX));
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX +
                            " 주소: " + server.getServerSocket().getInetAddress().getHostName() +
                            ":" + server.getServerSocket().getLocalPort()));
                } else {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 상태: &c꺼져 있음"));
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " &e/vosh 도움말"));
                for (String key : argsDescription.keySet()) {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX +
                            " /vosh " + key + " - " + argsDescription.get(key)));
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                server.refreshQueue();

                sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " &e보스 서버 클라이언트 목록"));
                sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " "
                        + server.getThreads().size() + "명 접속중"));
                for (ServerSideThread thread : server.getThreads()) {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " " + thread.getName()));
                }
            }
        }

        return false;
    }
}
