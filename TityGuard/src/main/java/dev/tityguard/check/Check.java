package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;

public abstract class Check {

    protected final TityGuard plugin;
    private final String name;

    protected Check(TityGuard plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("checks." + name + ".enabled", true);
    }

    public boolean setbackEnabled() {
        return plugin.getConfig().getBoolean("settings.setback", true)
                && plugin.getConfig().getBoolean("checks." + name + ".setback", true);
    }

    protected double cfg(String key, double def) {
        return plugin.getConfig().getDouble("checks." + name + "." + key, def);
    }

    /** Buffer'a delta ekler (0'in altina inmez), yeni degeri dondurur. */
    protected double buffer(PlayerData d, String sub, double delta) {
        String k = name + ":" + sub;
        double v = Math.max(0.0, d.buffers.getOrDefault(k, 0.0) + delta);
        d.buffers.put(k, v);
        return v;
    }

    protected void setBuffer(PlayerData d, String sub, double v) {
        d.buffers.put(name + ":" + sub, v);
    }

    protected void flag(Player p, String sub, String info) {
        plugin.violations().flag(p, this, sub + " | " + info, 1.0);
    }
}
