package dev.tomasgarcia.ultimatejoinplus;

import dev.tomasgarcia.ultimatejoinplus.commands.UltimateJoinCommand;
import dev.tomasgarcia.ultimatejoinplus.effects.EffectManager;
import dev.tomasgarcia.ultimatejoinplus.hooks.LuckPermsHook;
import dev.tomasgarcia.ultimatejoinplus.listeners.JoinQuitListener;
import dev.tomasgarcia.ultimatejoinplus.managers.*; // Import all
import dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class UltimateJoinPlus extends JavaPlugin {

        private static UltimateJoinPlus instance;
        private final Logger logger = getLogger();

        private BukkitAudiences adventure;
        private ConfigManager configManager;
        private LanguageManager languageManager;
        private LuckPermsHook luckPermsHook;

        private dev.tomasgarcia.ultimatejoinplus.data.DatabaseManager databaseManager;
        private dev.tomasgarcia.ultimatejoinplus.data.PlayerRepository repository;
        private EffectManager effectManager;
        private WelcomerManager welcomerManager;
        private SpawnManager spawnManager; // New
        private MaintenanceManager maintenanceManager; // New
        private RankManager rankManager;
        private JoinQuitListener joinQuitListener;

        @Override
        public void onEnable() {
                instance = this;
                this.adventure = BukkitAudiences.create(this);

                printStartupBanner();

                // 1. Initialize Managers
                logger.info("[UJ+] > Loading modules...");
                this.configManager = new ConfigManager(this);
                this.configManager.loadConfig();
                this.languageManager = new LanguageManager(this, configManager);
                saveResource("placeholders.yml", true);
                this.effectManager = new EffectManager(this);
                this.welcomerManager = new WelcomerManager(this, configManager, languageManager);
                this.spawnManager = new SpawnManager(this, configManager); // New
                this.spawnManager = new SpawnManager(this, configManager); // New
                this.maintenanceManager = new MaintenanceManager(this, configManager, languageManager); // New

                // 2. Initialize Hooks
                logger.info("[UJ+] > Hooking into LuckPerms...");
                this.luckPermsHook = new LuckPermsHook(this);
                this.luckPermsHook.hook();

                this.rankManager = new RankManager(configManager, luckPermsHook); // Moved after hook init

                // 3. Database
                if (getConfig().getBoolean("database.enabled", true)) {
                        logger.info("[UJ+] > Connecting to database...");
                        try {
                                this.databaseManager = new dev.tomasgarcia.ultimatejoinplus.data.DatabaseManager(this);
                                this.repository = new dev.tomasgarcia.ultimatejoinplus.data.PlayerRepository(
                                                databaseManager);
                                logger.info("[UJ+] > Database connected.");
                        } catch (Exception e) {
                                logger.severe("[UJ+] > Failed to connect to database!");
                                e.printStackTrace();
                        }
                }

                // 4. PAPI
                if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        new dev.tomasgarcia.ultimatejoinplus.papi.UltimateJoinExpansion(this).register();
                }

                // 5. Register Listeners
                this.joinQuitListener = new JoinQuitListener(this, configManager, effectManager,
                                languageManager, welcomerManager, rankManager);
                getServer().getPluginManager().registerEvents(joinQuitListener, this);
                getServer().getPluginManager().registerEvents(welcomerManager, this);
                getServer().getPluginManager().registerEvents(spawnManager, this); // New
                getServer().getPluginManager().registerEvents(maintenanceManager, this); // New

                // 6. Register Commands
                UltimateJoinCommand cmdExecutor = new UltimateJoinCommand(this, configManager, languageManager,
                                joinQuitListener, spawnManager, maintenanceManager);
                getCommand("uj").setExecutor(cmdExecutor);
                getCommand("spawn").setExecutor(cmdExecutor);
                getCommand("setspawn").setExecutor(cmdExecutor);
                getCommand("setfirstspawn").setExecutor(cmdExecutor);

                logger.info(ColorUtils.ANSI_GREEN + "[UJ+] > Plugin enabled successfully!" + ColorUtils.ANSI_RESET);
        }

        @Override
        public void onDisable() {
                if (this.adventure != null) {
                        this.adventure.close();
                        this.adventure = null;
                }
                if (this.databaseManager != null) {
                        this.databaseManager.close();
                }
                logger.info(ColorUtils.ANSI_RED + "[UJ+] > Plugin disabled." + ColorUtils.ANSI_RESET);
        }

        public static UltimateJoinPlus getInstance() {
                return instance;
        }

        public BukkitAudiences getAdventure() {
                return this.adventure;
        }

        public dev.tomasgarcia.ultimatejoinplus.data.PlayerRepository getRepository() {
                return repository;
        }

        public EffectManager getEffectManager() {
                return effectManager;
        }

        public LanguageManager getLanguageManager() {
                return languageManager;
        }

        public RankManager getRankManager() {
                return rankManager;
        }

        private void printStartupBanner() {
                // ... same banner ...
        }
}
