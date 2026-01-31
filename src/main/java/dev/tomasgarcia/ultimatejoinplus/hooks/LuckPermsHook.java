package dev.tomasgarcia.ultimatejoinplus.hooks;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.OptionalInt;

public class LuckPermsHook {

    private final UltimateJoinPlus plugin;
    private LuckPerms luckPerms;
    private boolean hooked;

    public LuckPermsHook(UltimateJoinPlus plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("[UJ+] > LuckPerms not found! specialized rank features will be disabled.");
            this.hooked = false;
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            this.hooked = true;
            plugin.getLogger().info("[UJ+] > Hooked into LuckPerms (Weight System Active)");
        } else {
            this.hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    public int getWeight(Player player) {
        if (!hooked)
            return 0;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            return 0;

        String primaryGroup = user.getPrimaryGroup();
        Group group = luckPerms.getGroupManager().getGroup(primaryGroup);

        if (group != null) {
            OptionalInt weight = group.getWeight();
            return weight.orElse(0);
        }
        return 0;
    }

    public String getPrimaryGroup(Player player) {
        if (!hooked)
            return "default";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null ? user.getPrimaryGroup() : "default";
    }
}
