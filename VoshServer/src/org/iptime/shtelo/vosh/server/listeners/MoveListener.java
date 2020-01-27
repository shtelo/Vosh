package org.iptime.shtelo.vosh.server.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.iptime.shtelo.vosh.server.Main;

public class MoveListener implements Listener {
    private Main plugin;

    public MoveListener(Main plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        double xRot = player.getLocation().getPitch();
        double yRot = player.getLocation().getYaw();

        // todo send data to all connected clients.
    }
}
