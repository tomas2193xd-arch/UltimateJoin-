package dev.tomasgarcia.ultimatejoinplus.effects;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EffectManager {

    private final UltimateJoinPlus plugin;

    public EffectManager(UltimateJoinPlus plugin) {
        this.plugin = plugin;
    }

    public void playEffect(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("custom-effects.enabled"))
            return;

        String type = config.getString("custom-effects.type", "HELIX").toUpperCase();
        String particleName = config.getString("custom-effects.particle", "FLAME");

        // Secondary particle for complex shapes
        String secondaryName = config.getString("custom-effects.secondary-particle", "END_ROD");

        Particle p1 = getParticle(particleName, Particle.FLAME);
        Particle p2 = getParticle(secondaryName, Particle.VILLAGER_HAPPY);

        switch (type) {
            case "TORNADO":
                playTornado(player, p1);
                break;
            case "WINGS":
                playWings(player, p1, p2);
                break;
            case "HALO":
                playHalo(player, p1);
                break;
            case "HELIX":
            default:
                playHelix(player, p1, p2);
                break;
        }
    }

    private Particle getParticle(String name, Particle def) {
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    // --- EFFECTS ---

    private void playWings(Player player, Particle core, Particle border) {
        // "Angel Wings" Shape
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (!player.isOnline() || step > 20) { // 1 second static display
                    this.cancel();
                    return;
                }

                Location loc = player.getLocation();
                double x, y, z;
                double yy = 0; // Height relative to player back

                // Rotate vectors based on player direction
                Vector dir = loc.getDirection().setY(0).normalize();
                Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();

                // Wing shape points (approximate)
                // We draw lines or points.
                boolean isRightWing = true;

                // Spawn a set of particles to form shape instant? Or animated?
                // Let's do Animated Draw

                drawWing(loc, right, 1); // Right Wing
                drawWing(loc, right, -1); // Left Wing

                step++;
            }

            private void drawWing(Location loc, Vector right, int side) {
                // Parametric Wing Curve logic simplified
                for (double i = 0; i < 1.5; i += 0.2) {
                    // Curve math
                    double mold = i * (2 - i); // Simple curve

                    Vector offset = right.clone().multiply(side * (0.5 + i));
                    offset.setY(0.8 + mold); // Arch up

                    // Push back slightly
                    Vector back = loc.getDirection().multiply(-0.3);

                    Location target = loc.clone().add(offset).add(back);
                    spawnParticle(player, core, target);

                    if (i > 1.2)
                        spawnParticle(player, border, target); // Tips
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void playHalo(Player player, Particle p) {
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline() || angle > Math.PI * 4) {
                    this.cancel();
                    return;
                }

                Location head = player.getEyeLocation().add(0, 0.5, 0);
                double radius = 0.4;

                // Draw full circle instantly? Or spin? Let's Spin fast
                for (int i = 0; i < 3; i++) { // 3 particles per tick
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    head.add(x, 0, z);
                    spawnParticle(player, p, head);
                    head.subtract(x, 0, z); // Reset for next calc
                    angle += 0.3;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playHelix(Player player, Particle p1, Particle p2) {
        new BukkitRunnable() {
            double t = 0;
            final double radius = 1.2;

            @Override
            public void run() {
                if (!player.isOnline() || t > 2 * Math.PI * 2) {
                    this.cancel();
                    return;
                }

                for (double y = 0; y <= 2; y += 0.1) {
                    double angle = t + y * 2;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Location loc1 = player.getLocation().clone().add(x, y, z);
                    Location loc2 = player.getLocation().clone().add(-x, y, -z);

                    spawnParticle(player, p1, loc1);
                    spawnParticle(player, p2, loc2);
                }
                t += Math.PI / 8;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playTornado(Player player, Particle p1) {
        new BukkitRunnable() {
            double angle = 0;
            double y = 0;
            double radius = 0.5;

            @Override
            public void run() {
                if (!player.isOnline() || y > 3) {
                    this.cancel();
                    return;
                }
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location loc = player.getLocation().clone().add(x, y, z);
                spawnParticle(player, p1, loc);
                y += 0.1;
                angle += 0.5;
                radius += 0.05;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnParticle(Player p, Particle particle, Location loc) {
        try {
            // Display only to player and nearby? standard spawnParticle does this
            // implicitly for "player" world context?
            // Actually p.spawnParticle emits to the player. p.getWorld().spawnParticle
            // emits to everyone.
            // Cosmetics usually everyone sees.
            if (loc.getWorld() == null)
                return;
            loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        } catch (Exception ignored) {
        }
    }
}
