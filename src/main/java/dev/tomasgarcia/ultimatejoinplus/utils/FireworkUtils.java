package dev.tomasgarcia.ultimatejoinplus.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireworkUtils {

    private static final Random random = new Random();

    public static void spawnRandomFirework(Location location, int power) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        // Random Type
        FireworkEffect.Type type = FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)];

        // Random Colors
        Color c1 = getRandomColor();
        Color c2 = getRandomColor();

        // Create Effect
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(c1)
                .withFade(c2)
                .with(type)
                .trail(random.nextBoolean())
                .build();

        fwm.addEffect(effect);
        fwm.setPower(power); // 1-3

        fw.setFireworkMeta(fwm);
    }

    private static Color getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return Color.fromRGB(r, g, b);
    }
}
