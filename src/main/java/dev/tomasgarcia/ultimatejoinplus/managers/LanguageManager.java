package dev.tomasgarcia.ultimatejoinplus.managers;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final UltimateJoinPlus plugin;
    private final ConfigManager configManager;
    private FileConfiguration infoConfig;
    private File infoFile;
    private String lang;

    private final Map<String, String> messageCache = new HashMap<>();

    public LanguageManager(UltimateJoinPlus plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadLanguage();
    }

    public void loadLanguage() {
        this.lang = configManager.getConfig().getString("settings.locale", "en");
        String fileName = "messages_" + lang + ".yml";

        this.infoFile = new File(plugin.getDataFolder(), fileName);

        // Save resource if not exists
        if (!infoFile.exists()) {
            // Fallback: check if the resource internally exists
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                plugin.getLogger().warning("Language file " + fileName + " not found in JAR! Defaulting to English.");
                plugin.saveResource("messages_en.yml", false);
                this.infoFile = new File(plugin.getDataFolder(), "messages_en.yml");
            }
        }

        this.infoConfig = YamlConfiguration.loadConfiguration(infoFile);

        // Load defaults from JAR to ensure new keys exist
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            infoConfig.setDefaults(defConfig);
        }

        // Cache messages for performance
        messageCache.clear();
        for (String key : infoConfig.getKeys(true)) {
            if (infoConfig.isString(key)) {
                messageCache.put(key, infoConfig.getString(key));
            }
        }

        plugin.getLogger().info("Loaded language: " + lang);
    }

    public void reload() {
        loadLanguage();
    }

    public String getMessage(String key) {
        String msg = messageCache.get(key);
        if (msg == null) {
            return ""; // Soft fallback to empty string logic in listener, or specific error
        }
        return ColorUtils.translateLegacy(msg);
    }

    public java.util.List<String> getStringList(String key) {
        if (infoConfig.isList(key)) {
            return infoConfig.getStringList(key);
        }
        return null;
    }
}
