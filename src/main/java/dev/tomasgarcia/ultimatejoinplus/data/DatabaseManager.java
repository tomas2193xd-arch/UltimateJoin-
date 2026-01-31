package dev.tomasgarcia.ultimatejoinplus.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final UltimateJoinPlus plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(UltimateJoinPlus plugin) {
        this.plugin = plugin;
        setupDataSource();
        initTables();
    }

    private void setupDataSource() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite");

        HikariConfig hikariConfig = new HikariConfig();

        if (type.equalsIgnoreCase("mysql")) {
            String host = config.getString("database.host", "localhost");
            String port = config.getString("database.port", "3306");
            String database = config.getString("database.database", "ultimatejoin");
            String username = config.getString("database.username", "root");
            String password = config.getString("database.password", "");

            hikariConfig.setJdbcUrl(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            // SQLite
            String path = plugin.getDataFolder().getAbsolutePath() + "/database.db";
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + path);
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }

        hikariConfig.setPoolName("UltimateJoinPlusPool");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(30000);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private void initTables() {
        String query = "CREATE TABLE IF NOT EXISTS ujp_players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(16), " +
                "joins INT DEFAULT 0, " +
                "first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
