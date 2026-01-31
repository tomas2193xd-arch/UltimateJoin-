package dev.tomasgarcia.ultimatejoinplus.data;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository {

    private final DatabaseManager dbManager;

    public PlayerRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dbManager.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM ujp_players WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return new PlayerData(
                            rs.getString("uuid"),
                            rs.getString("name"),
                            rs.getInt("joins"),
                            rs.getTimestamp("first_join"),
                            rs.getTimestamp("last_join"));
                }
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Integer> getTotalUniquePlayers() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dbManager.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM ujp_players");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    public void savePlayerData(PlayerData data) {
        Bukkit.getScheduler().runTaskAsynchronously(UltimateJoinPlus.getInstance(), () -> {
            try (Connection conn = dbManager.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "REPLACE INTO ujp_players (uuid, name, joins, first_join, last_join) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, data.getUuid());
                ps.setString(2, data.getName());
                ps.setInt(3, data.getJoins());
                ps.setTimestamp(4, data.getFirstJoin());
                ps.setTimestamp(5, data.getLastJoin());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateJoinData(Player player) {
        getPlayerData(player.getUniqueId()).thenAccept(data -> {
            long now = System.currentTimeMillis();
            if (data == null) {
                // First join
                data = new PlayerData(
                        player.getUniqueId().toString(),
                        player.getName(),
                        1,
                        new Timestamp(now),
                        new Timestamp(now));
            } else {
                data.incrementJoins();
                data.setLastJoin(new Timestamp(now));
            }
            savePlayerData(data);
        });
    }
}
