package org.iptime.shtelo.vosh.server;

import org.bukkit.plugin.java.JavaPlugin;
import org.iptime.shtelo.vosh.server.commands.VoshCommand;
import org.iptime.shtelo.vosh.server.listeners.MoveListener;
import org.iptime.shtelo.vosh.server.web.Server;

import java.io.IOException;

public class Main extends JavaPlugin {
    private Server server;

    @Override
    public void onEnable() {
        server = new Server(this);

        new VoshCommand(server, this);

        new MoveListener(this);

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        server.stop();
    }
}
