package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WelcomerManager implements Listener {

    private final UltimateJoinPlus plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;

    private boolean eventActive = false;
    private Set<UUID> rewardedPlayers = new HashSet<>();
    private long eventEndTime;

    public WelcomerManager(UltimateJoinPlus plugin, ConfigManager configManager, LanguageManager languageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.languageManager = languageManager;
    }

    public void startEvent(Player newPlayer) {
        if (!configManager.getConfig().getBoolean("events.welcomer.enabled"))
            return;

        eventActive = true;
        rewardedPlayers.clear();

        int duration = configManager.getConfig().getInt("events.welcomer.duration", 15);
        this.eventEndTime = System.currentTimeMillis() + (duration * 1000L);

        // Broadcast Message
        Bukkit.broadcast(MessageProcessor.process(languageManager.getMessage("welcomer-start")));

        // End Task
        new BukkitRunnable() {
            @Override
            public void run() {
                eventActive = false;
                rewardedPlayers.clear();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!eventActive)
            return;
        if (System.currentTimeMillis() > eventEndTime) {
            eventActive = false;
            return;
        }

        Player player = event.getPlayer();
        if (rewardedPlayers.contains(player.getUniqueId()))
            return;

        String message = event.getMessage().toLowerCase();
        List<String> validWords = configManager.getConfig().getStringList("events.welcomer.words");

        boolean matched = false;
        for (String word : validWords) {
            if (message.contains(word.toLowerCase())) {
                matched = true;
                break;
            }
        }

        if (matched) {
            rewardedPlayers.add(player.getUniqueId());
            // Give Rewards (Sync)
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<String> rewards = configManager.getConfig().getStringList("events.welcomer.rewards");
                for (String reward : rewards) {
                    if (reward.startsWith("msg ")) {
                        String msg = reward.substring(4).replace("%player%", player.getName());
                        player.sendMessage(MessageProcessor.process(player, msg));
                    } else {
                        String cmd = reward.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                }
                player.sendMessage(MessageProcessor.process(languageManager.getMessage("welcomer-reward")));
            });
        }
    }
}
