package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class MaintenanceManager implements Listener {

    private final ConfigManager configManager;
    private final LanguageManager languageManager;

    public MaintenanceManager(UltimateJoinPlus plugin, ConfigManager configManager, LanguageManager languageManager) {
        this.configManager = configManager;
        this.languageManager = languageManager;
    }

    public void setMaintenance(boolean enabled) {
        configManager.getConfig().set("maintenance.enabled", enabled);
        configManager.saveConfig();

        if (enabled) {
            kickPlayers();
        }
    }

    public boolean isMaintenance() {
        return configManager.getConfig().getBoolean("maintenance.enabled", false);
    }

    private void kickPlayers() {
        String kickMsg = MessageProcessor.parse(null, languageManager.getMessage("maintenance-kick"));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("uj.bypass")) {
                p.kickPlayer(kickMsg); // Legacy kick for compatibility
            }
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (!isMaintenance())
            return;

        // We can't check permissions easily in AsyncPreLogin without Vault or UUID
        // lookup
        // But we can check if they are whitelisted or simple name check if needed
        // For permissions, we often wait for LoginEvent, but PreLogin is safer to stop
        // connection.
        // A compromise for standard servers: LoginEvent allows perm check.
    }

    // Changing to PlayerLoginEvent to allow permission check
    @EventHandler
    public void onPlayerLogin(org.bukkit.event.player.PlayerLoginEvent event) {
        if (!isMaintenance())
            return;

        Player p = event.getPlayer();
        if (!p.hasPermission("uj.bypass")) {
            String kickMsg = MessageProcessor.parse(null, languageManager.getMessage("maintenance-kick"));
            event.disallow(org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER, kickMsg);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (isMaintenance()) {
            String motd = MessageProcessor.parse(null, languageManager.getMessage("maintenance-motd"));
            event.setMotd(motd);
        }
    }
}
