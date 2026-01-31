package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.hooks.LuckPermsHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RankManager {

    private final ConfigManager configManager;
    private final LuckPermsHook luckPermsHook;

    public RankManager(ConfigManager configManager, LuckPermsHook luckPermsHook) {
        this.configManager = configManager;
        this.luckPermsHook = luckPermsHook;
    }

    /**
     * Determines the best rank key for a player based on configuration weights and
     * permissions.
     * 
     * @param player The player to check
     * @return The key of the rank group (e.g., "vip", "msg"), or "default"
     */
    public String getBestRank(Player player) {
        FileConfiguration config = configManager.getConfig();
        String groupKey = "default";
        int highestWeight = -1;

        if (config.contains("ranks")) {
            for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
                ConfigurationSection rankSection = config.getConfigurationSection("ranks." + key);
                if (rankSection == null)
                    continue;

                int weight = rankSection.getInt("weight", 0);
                String perm = rankSection.getString("permission");

                boolean matches = false;

                // 1. Check LuckPerms Primary Group
                if (luckPermsHook.isHooked()) {
                    if (luckPermsHook.getPrimaryGroup(player).equalsIgnoreCase(key)) {
                        matches = true;
                    }
                }

                // 2. Check Permission Hook/Override
                // If they have the permission, it matches regardless of LP group
                if (perm != null && player.hasPermission(perm)) {
                    matches = true;
                }

                if (matches && weight > highestWeight) {
                    highestWeight = weight;
                    groupKey = key;
                }
            }
        }
        return groupKey;
    }

    public String getPrimaryGroup(Player player) {
        if (luckPermsHook.isHooked()) {
            return luckPermsHook.getPrimaryGroup(player);
        }
        return "default";
    }
}
