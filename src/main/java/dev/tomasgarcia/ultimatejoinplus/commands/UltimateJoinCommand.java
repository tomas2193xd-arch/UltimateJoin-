package dev.tomasgarcia.ultimatejoinplus.commands;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import dev.tomasgarcia.ultimatejoinplus.listeners.JoinQuitListener;
import dev.tomasgarcia.ultimatejoinplus.managers.*;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class UltimateJoinCommand implements CommandExecutor {

    private final UltimateJoinPlus plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final JoinQuitListener joinQuitListener;
    private final SpawnManager spawnManager;
    private final MaintenanceManager maintenanceManager;

    public UltimateJoinCommand(UltimateJoinPlus plugin, ConfigManager configManager, LanguageManager languageManager,
            JoinQuitListener joinQuitListener, SpawnManager spawnManager, MaintenanceManager maintenanceManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.languageManager = languageManager;
        this.joinQuitListener = joinQuitListener;
        this.spawnManager = spawnManager;
        this.maintenanceManager = maintenanceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        // ALIASES for Spawn logic if command label is /spawn, /setspawn etc.
        // We need to register these in plugin.yml as aliases or separate commands
        // pointing here.
        // But typical "setExecutor" works on one command.
        // We will assume Main registered this executor for "uj", "spawn", "setspawn",
        // "setfirstspawn".

        String cleanLabel = label.toLowerCase();

        if (cleanLabel.equals("spawn")) {
            if (sender instanceof Player) {
                spawnManager.teleport((Player) sender, "main");
                audience.sendMessage(MessageProcessor.process(languageManager.getMessage("teleport-spawn")));
            }
            return true;
        }
        if (cleanLabel.equals("setspawn")) {
            if (!sender.hasPermission("uj.setspawn")) {
                audience.sendMessage(MessageProcessor.process(languageManager.getMessage("no-permission")));
                return true;
            }
            if (sender instanceof Player) {
                spawnManager.setSpawn((Player) sender, "main");
                audience.sendMessage(MessageProcessor.process(languageManager.getMessage("spawn-set")));
            }
            return true;
        }
        if (cleanLabel.equals("setfirstspawn")) {
            if (!sender.hasPermission("uj.setspawn"))
                return true;
            if (sender instanceof Player) {
                spawnManager.setSpawn((Player) sender, "first");
                audience.sendMessage(MessageProcessor.process(languageManager.getMessage("first-spawn-set")));
            }
            return true;
        }

        // Logic for /uj
        if (args.length == 0) {
            sendDynamicHelp(audience);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("uj.admin")) {
                    audience.sendMessage(MessageProcessor.process(languageManager.getMessage("no-permission")));
                    return true;
                }
                configManager.loadConfig();
                languageManager.reload();
                audience.sendMessage(MessageProcessor
                        .process(languageManager.getMessage("prefix") + languageManager.getMessage("reload-success")));
                break;
            case "info":
                audience.sendMessage(
                        MessageProcessor.process("<gradient:#00AAFF:#0055AA>UltimateJoin+</gradient> <gray>v"
                                + plugin.getDescription().getVersion()));
                break;
            case "fakejoin":
                if (sender instanceof Player && sender.hasPermission("uj.admin")) {
                    joinQuitListener.handleJoinLogic((Player) sender);
                }
                break;
            case "fakequit":
                if (sender instanceof Player && sender.hasPermission("uj.admin")) {
                    joinQuitListener.handleQuitLogic((Player) sender);
                }
                break;
            case "maintenance":
                if (!sender.hasPermission("uj.maintenance")) {
                    audience.sendMessage(MessageProcessor.process(languageManager.getMessage("no-permission")));
                    return true;
                }
                if (args.length > 1) {
                    boolean enable = args[1].equalsIgnoreCase("on");
                    maintenanceManager.setMaintenance(enable);
                    if (enable)
                        audience.sendMessage(
                                MessageProcessor.process(languageManager.getMessage("maintenance-enabled")));
                    else
                        audience.sendMessage(
                                MessageProcessor.process(languageManager.getMessage("maintenance-disabled")));
                } else {
                    audience.sendMessage(MessageProcessor.process("<red>Usage: /uj maintenance <on/off>"));
                }
                break;
            default:
                sendDynamicHelp(audience);
                break;
        }
        return true;
    }

    private void sendDynamicHelp(Audience audience) {
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-header")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-spawn")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-setspawn")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-setfirstspawn")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-maintenance")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-reload")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-fakejoin")));
        audience.sendMessage(MessageProcessor.process(languageManager.getMessage("help-fakequit")));
    }
}
