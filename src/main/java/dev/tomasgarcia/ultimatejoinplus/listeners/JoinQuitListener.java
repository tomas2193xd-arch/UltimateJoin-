/*
 *  UltimateJoin+ - The most advanced welcome plugin.
 *  Copyright (c) 2026 Tomas Garcia
 *  All Rights Reserved.
 */
package dev.tomasgarcia.ultimatejoinplus.listeners;

import com.cryptomorin.xseries.XSound;
import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import dev.tomasgarcia.ultimatejoinplus.effects.EffectManager;
import dev.tomasgarcia.ultimatejoinplus.managers.*;
import dev.tomasgarcia.ultimatejoinplus.utils.MessageUtils;
import dev.tomasgarcia.ultimatejoinplus.utils.SmartErrorHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JoinQuitListener implements Listener {

    private final UltimateJoinPlus plugin;
    private final ConfigManager configManager;
    private final EffectManager effectManager;
    private final LanguageManager languageManager;
    private final WelcomerManager welcomerManager;
    private final RankManager rankManager;

    private final List<Long> recentJoins = new ArrayList<>();

    public JoinQuitListener(UltimateJoinPlus plugin, ConfigManager configManager,
            EffectManager effectManager, LanguageManager languageManager,
            WelcomerManager welcomerManager, RankManager rankManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.effectManager = effectManager;
        this.languageManager = languageManager;
        this.welcomerManager = welcomerManager;
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        // Update database asynchronously (Safe)
        if (plugin.getRepository() != null) {
            plugin.getRepository().updateJoinData(player);
        }

        // Run join logic Synchronously (Safe for Bukkit API)
        try {
            handleJoinLogic(player);
        } catch (Exception e) {
            SmartErrorHandler.handleConfigError("Join Event Processing", e);
        }

        // Play Effect
        effectManager.playEffect(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        handleQuitLogic(event.getPlayer());
    }

    public void handleJoinLogic(Player player) {
        FileConfiguration config = configManager.getConfig();
        long now = System.currentTimeMillis();
        Audience audience = plugin.getAdventure().player(player);
        Audience serverAudience = plugin.getAdventure().all();

        cleanThrottleList(now);
        recentJoins.add(now);

        // Throttle Logic
        int throttleThreshold = config.getInt("settings.throttle-threshold", 5);
        if (recentJoins.size() >= throttleThreshold) {
            String throttleMsg = languageManager.getMessage("throttle-broadcast");
            throttleMsg = throttleMsg.replace("<count>", String.valueOf(recentJoins.size()));
            serverAudience.sendMessage(MessageProcessor.process(player, throttleMsg));
            return;
        }

        // Determine Grant (Rank key)
        String groupKey = rankManager.getBestRank(player);

        if (!player.hasPlayedBefore()) {
            handleFirstJoin(player, serverAudience, config);
        }

        // Time Greetings
        handleTimeGreeting(player, audience, config);

        // Join Message
        ConfigurationSection rankConfig = config.getConfigurationSection("ranks." + groupKey);
        if (rankConfig == null)
            rankConfig = config.getConfigurationSection("ranks.default");

        List<String> lines = languageManager.getStringList("rank-join-" + groupKey);
        if (lines == null || lines.isEmpty()) {
            if (rankConfig.isList("join-message")) {
                lines = rankConfig.getStringList("join-message");
            } else {
                lines = new ArrayList<>();
                lines.add(rankConfig.getString("join-message"));
            }
        }

        for (String line : lines) {
            if (line.contains("<center>")) {
                String text = line.replace("<center>", "");
                serverAudience.sendMessage(MessageUtils.centerMessage(MessageProcessor.parse(player, text)));
            } else {
                serverAudience.sendMessage(MessageProcessor.process(player, line));
            }
        }

        // Action Execution (Titles, Sounds, BossBars)
        executeRankActions(player, rankConfig, groupKey);
    }

    private void handleFirstJoin(Player player, Audience serverAudience, FileConfiguration config) {
        // Welcomer Event
        welcomerManager.startEvent(player);

        // Console Commands Rewards
        if (config.getBoolean("first-join.commands.enabled")) {
            List<String> cmds = config.getStringList("first-join.commands.list");
            for (String cmd : cmds) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }

        if (config.getBoolean("first-join.broadcast")) {
            List<String> broadcast = languageManager.getStringList("first-join-broadcast");
            if (broadcast == null || broadcast.isEmpty()) {
                broadcast = config.getStringList("first-join.broadcast");
            }
            for (String line : broadcast) {
                if (line.contains("<center>")) {
                    String text = line.replace("<center>", "");
                    serverAudience.sendMessage(MessageUtils.centerMessage(MessageProcessor.parse(player, text)));
                } else {
                    serverAudience.sendMessage(MessageProcessor.process(player, line));
                }
            }
        }
    }

    private void handleTimeGreeting(Player player, Audience audience, FileConfiguration config) {
        if (config.getBoolean("time-greetings.enabled", false)) {
            long time = player.getWorld().getTime();
            String timeMsgKey = (time >= 0 && time <= 12000) ? "greeting-morning" : "greeting-night";
            String timeMsg = languageManager.getMessage(timeMsgKey);

            if (timeMsg != null && !timeMsg.isEmpty()) {
                if (timeMsg.contains("<center>")) {
                    String text = timeMsg.replace("<center>", "");
                    audience.sendMessage(MessageUtils.centerMessage(MessageProcessor.parse(player, text)));
                } else {
                    audience.sendMessage(MessageProcessor.process(player, timeMsg));
                }
            }
        }
    }

    private void executeRankActions(Player player, ConfigurationSection rankConfig, String groupKey) {
        FileConfiguration config = configManager.getConfig();
        Audience audience = plugin.getAdventure().player(player);

        if (rankConfig.contains("sound") && config.getBoolean("features.sounds")) {
            String soundName = rankConfig.getString("sound");
            XSound.matchXSound(soundName).ifPresent(xSound -> xSound.play(player));
            // Also play just for player or for all? Original code played for everyone AND
            // player
            for (Player p : Bukkit.getOnlinePlayers()) {
                XSound.matchXSound(soundName).ifPresent(xSound -> xSound.play(p));
            }
        }

        if (rankConfig.contains("title") && config.getBoolean("features.titles")) {
            String title = languageManager.getMessage("rank-title-" + groupKey);
            String subtitle = languageManager.getMessage("rank-subtitle-" + groupKey);

            if (title.isEmpty())
                title = rankConfig.getString("title.title", "");
            if (subtitle.isEmpty())
                subtitle = rankConfig.getString("title.subtitle", "");

            Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000),
                    Duration.ofMillis(1000));
            Title t = Title.title(MessageProcessor.process(player, title), MessageProcessor.process(player, subtitle),
                    times);
            audience.showTitle(t);
        }

        if (rankConfig.contains("bossbar") && config.getBoolean("features.bossbar")) {
            String barTx = languageManager.getMessage("rank-bossbar-" + groupKey);
            if (barTx.isEmpty())
                barTx = rankConfig.getString("bossbar.text", "Welcome");

            String colorStr = rankConfig.getString("bossbar.color", "WHITE");
            String styleStr = rankConfig.getString("bossbar.style", "SOLID");

            BossBar.Color color;
            try {
                color = BossBar.Color.valueOf(colorStr);
            } catch (IllegalArgumentException e) {
                color = BossBar.Color.WHITE;
            }

            BossBar.Overlay style;
            if (styleStr.equalsIgnoreCase("SOLID")) {
                style = BossBar.Overlay.PROGRESS;
            } else {
                try {
                    style = BossBar.Overlay.valueOf(styleStr);
                } catch (IllegalArgumentException e) {
                    style = BossBar.Overlay.PROGRESS;
                }
            }

            BossBar bar = BossBar.bossBar(MessageProcessor.process(player, barTx), 1.0f, color, style);
            audience.showBossBar(bar);

            Bukkit.getScheduler().runTaskLater(plugin, () -> audience.hideBossBar(bar),
                    rankConfig.getInt("bossbar.duration", 10) * 20L);
        }
    }

    public void handleQuitLogic(Player player) {
        String group = rankManager.getPrimaryGroup(player);
        String msg = languageManager.getMessage("rank-quit-" + group);

        if (msg.isEmpty()) {
            FileConfiguration config = configManager.getConfig();
            msg = config.getString("ranks." + group + ".quit-message");
            if (msg == null || msg.isEmpty()) {
                msg = config.getString("ranks.default.quit-message", "<gray>" + player.getName() + " left.");
            }
        }
        plugin.getAdventure().all().sendMessage(MessageProcessor.process(player, msg));
    }

    private void cleanThrottleList(long now) {
        int window = configManager.getConfig().getInt("settings.throttle-window", 3) * 1000;
        recentJoins.removeIf(time -> (now - time) > window);
    }
}
