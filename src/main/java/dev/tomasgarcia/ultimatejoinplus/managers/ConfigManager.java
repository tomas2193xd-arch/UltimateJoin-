package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final UltimateJoinPlus plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(UltimateJoinPlus plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("[UJ+] Config loaded successfully.");

        // Here we will call cache methods later
    }

    public void reloadConfig() {
        loadConfig();
        // Re-cache data here
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
