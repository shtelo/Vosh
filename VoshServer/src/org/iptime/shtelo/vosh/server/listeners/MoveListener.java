package org.iptime.shtelo.vosh.server.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.iptime.shtelo.vosh.server.Main;

import java.util.HashMap;

public class MoveListener implements Listener {
    private HashMap<String, double[]> data;

    public MoveListener(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        data = new HashMap<>();  // "Sch_0q0" -> [x, y, z, yr]
    }

    public double getYaw(String player) {
        assert data.containsKey(player);
        return data.get(player)[3];
    }

    public double[] getPosition(String player) {
        assert data.containsKey(player);
        return data.get(player);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        double yRot = player.getLocation().getYaw();

        data.put(player.getDisplayName(), new double[]{x, y, z, yRot});
    }
}
