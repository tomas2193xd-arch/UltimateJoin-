package dev.tomasgarcia.ultimatejoinplus.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageProcessor {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static boolean papiPresent;

    static {
        papiPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static Component process(CommandSender sender, String message) {
        if (message == null)
            return Component.empty();
        return miniMessage.deserialize(parse(sender, message));
    }

    public static String parse(CommandSender sender, String message) {
        if (message == null)
            return "";

        // 1. Translate Legacy Colors first
        message = dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils.translateLegacy(message);

        if (papiPresent && sender instanceof Player) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) sender, message);
        }
        // Fallback or self-replace
        if (sender != null) {
            message = message.replace("%player_name%", sender.getName());
        }
        return message;
    }

    public static Component process(Player player, String message) {
        return process((CommandSender) player, message);
    }

    public static String parse(Player player, String message) {
        return parse((CommandSender) player, message);
    }

    public static Component process(String message) {
        // Simple process without placeholders
        message = dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils.translateLegacy(message);
        return miniMessage.deserialize(message);
    }
}
