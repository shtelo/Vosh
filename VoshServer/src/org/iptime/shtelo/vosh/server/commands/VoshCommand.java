package org.iptime.shtelo.vosh.server.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.iptime.shtelo.vosh.server.Main;
import org.iptime.shtelo.vosh.server.utils.Constants;
import org.iptime.shtelo.vosh.server.utils.Utils;
import org.iptime.shtelo.vosh.server.web.Server;

import java.io.IOException;
import java.util.Objects;

public class VoshCommand implements CommandExecutor {
    private Server server;
    private Main plugin;

    public VoshCommand(Server server, Main plugin) {
        this.server = server;
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("vosh")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (server.isRunning()) {
                    server.stop();
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버를 종료했습니다."));
                    return false;
                } else {
                    sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버가 이미 꺼져 있습니다!"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("start")) {
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
                        sender.sendMessage(Utils.chat(Constants.CHATTING_PREFIX + " 서버를 여는 중 오류가 발생했습니다!"));
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
