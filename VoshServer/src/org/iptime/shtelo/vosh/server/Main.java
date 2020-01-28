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
        MoveListener moveListener = new MoveListener(this);

        server = new Server(moveListener, this);

        new VoshCommand(server, this);

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
