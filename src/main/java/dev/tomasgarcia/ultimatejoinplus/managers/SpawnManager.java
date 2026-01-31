package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnManager implements Listener {

    private final UltimateJoinPlus plugin;
    private final ConfigManager configManager;

    public SpawnManager(UltimateJoinPlus plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void setSpawn(Player player, String type) {
        Location loc = player.getLocation();
        FileConfiguration config = configManager.getConfig();
        String path = type.equals("first") ? "spawn.first-location" : "spawn.location";

        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());

        configManager.saveConfig();
    }

    public Location getSpawn(String type) {
        FileConfiguration config = configManager.getConfig();
        String path = type.equals("first") ? "spawn.first-location" : "spawn.location";

        if (!config.contains(path + ".world"))
            return null;

        String worldName = config.getString(path + ".world");
        if (Bukkit.getWorld(worldName) == null)
            return null;

        return new Location(
                Bukkit.getWorld(worldName),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch"));
    }

    public void teleport(Player player, String type) {
        Location loc = getSpawn(type);
        if (loc != null) {
            player.teleport(loc);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPlayedBefore()) {
            if (configManager.getConfig().getBoolean("first-join.teleport-to-spawn", true)) {
                Location first = getSpawn("first");
                if (first != null)
                    p.teleport(first);
                else
                    teleport(p, "main");
            }
        } else {
            if (configManager.getConfig().getBoolean("spawn.teleport-on-join", true)) {
                teleport(p, "main");
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (configManager.getConfig().getBoolean("spawn.teleport-on-respawn", true)) {
            Location loc = getSpawn("main");
            if (loc != null) {
                event.setRespawnLocation(loc);
            }
        }
    }
}
