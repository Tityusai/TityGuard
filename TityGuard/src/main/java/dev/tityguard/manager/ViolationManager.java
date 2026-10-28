package dev.tityguard.manager;

import dev.tityguard.TityGuard;
import dev.tityguard.check.Check;
import dev.tityguard.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    private final TityGuard plugin;
    private final Map<UUID, Map<String, Double>> vls = new ConcurrentHashMap<>();

    public ViolationManager(TityGuard plugin) {
        this.plugin = plugin;
    }

    public void flag(Player p, Check check, String info, double amount) {
        Map<String, Double> map = vls.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>());
        double vl = map.merge(check.getName(), amount, Double::sum);

        String format = plugin.getConfig().getString("settings.alert-format",
                "&8[&bTity&3Guard&8] &f%player% &7failed &c%check% &8(&7VL %vl%&8) &7%info%");
        String msg = ChatColor.translateAlternateColorCodes('&', format
                .replace("%player%", p.getName())
                .replace("%check%", check.getName())
                .replace("%vl%", String.format("%.0f", vl))
                .replace("%info%", info));

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission("tityguard.alerts")) continue;
            PlayerData sd = plugin.data(staff);
            if (sd.alerts) staff.sendMessage(msg);
        }
        if (plugin.getConfig().getBoolean("settings.alert-console", true)) {
            plugin.getLogger().info(ChatColor.stripColor(msg));
        }

        double max = plugin.getConfig().getDouble("checks." + check.getName() + ".max-vl",
                plugin.getConfig().getDouble("punishment.default-vl", 25));
        if (plugin.getConfig().getBoolean("settings.punish", true) && vl >= max) {
            map.put(check.getName(), 0.0);
            List<String> cmds = plugin.getConfig().getStringList("punishment.commands");
            for (String c : cmds) {
                String run = ChatColor.translateAlternateColorCodes('&', c
                        .replace("%player%", p.getName())
                        .replace("%check%", check.getName()));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), run);
            }
        }
    }

    public Map<String, Double> get(UUID id) {
        Map<String, Double> m = vls.get(id);
        return m == null ? new HashMap<>() : new HashMap<>(m);
    }

    public void reset(UUID id) {
        vls.remove(id);
    }

    public void startDecayTask() {
        long period = 20L * plugin.getConfig().getLong("settings.decay-interval-seconds", 30L);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double amount = plugin.getConfig().getDouble("settings.decay-amount", 1.0);
            for (Map<String, Double> m : vls.values()) {
                m.replaceAll((k, v) -> Math.max(0.0, v - amount));
            }
        }, period, period);
    }
}
