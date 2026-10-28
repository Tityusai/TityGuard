package dev.tityguard;

import dev.tityguard.check.CheckManager;
import dev.tityguard.command.TityCommand;
import dev.tityguard.data.PlayerData;
import dev.tityguard.listener.CombatListener;
import dev.tityguard.listener.ConnectionListener;
import dev.tityguard.listener.MovementListener;
import dev.tityguard.listener.WorldListener;
import dev.tityguard.manager.ViolationManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TityGuard extends JavaPlugin {

    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    private ViolationManager violations;
    private CheckManager checks;

    private long lastTick = System.currentTimeMillis();
    private volatile long lastLag = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.violations = new ViolationManager(this);
        this.checks = new CheckManager(this);

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        TityCommand cmd = new TityCommand(this);
        getCommand("tg").setExecutor(cmd);
        getCommand("tg").setTabCompleter(cmd);

        for (Player p : getServer().getOnlinePlayers()) data(p);

        violations.startDecayTask();

        // sunucu lag tespiti (timer check yanlis flag atmasin)
        getServer().getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            if (now - lastTick > 150) lastLag = now;
            lastTick = now;
        }, 1L, 1L);

        getLogger().info("TityGuard enabled.");
    }

    @Override
    public void onDisable() {
        dataMap.clear();
    }

    public PlayerData data(Player p) {
        return dataMap.computeIfAbsent(p.getUniqueId(), PlayerData::new);
    }

    public void removeData(UUID id) {
        dataMap.remove(id);
    }

    public boolean recentLag(long now, long windowMs) {
        return now - lastLag < windowMs;
    }

    public ViolationManager violations() { return violations; }
    public CheckManager checks() { return checks; }
}
