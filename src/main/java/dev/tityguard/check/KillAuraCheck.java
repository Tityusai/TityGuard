package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class KillAuraCheck extends Check {

    public KillAuraCheck(TityGuard plugin) {
        super(plugin, "killaura");
    }

    public boolean handle(Player p, PlayerData d, LivingEntity victim, long now) {
        if (!isEnabled()) return false;
        boolean flagged = false;

        // 1) Nisan / hitbox: bakis isini hedefin (ping payi eklenmis) kutusuna carpiyor mu?
        double expand = cfg("hitbox-expand", 0.2) + Math.min(p.getPing(), 250) * cfg("ping-comp", 0.0015);
        BoundingBox box = victim.getBoundingBox().expand(expand);
        Location eye = p.getEyeLocation();
        RayTraceResult r = box.rayTrace(eye.toVector(), eye.getDirection(), 8.0);
        double aimBuf = cfg("aim-buffer", 5);
        if (r == null) {
            if (buffer(d, "aim", 1.0) > aimBuf) {
                flag(p, "aim", String.format("not looking at target (expand=%.2f, ping=%d)", expand, p.getPing()));
                setBuffer(d, "aim", aimBuf / 2);
                flagged = true;
            }
        } else {
            buffer(d, "aim", -0.5);
        }

        // 2) Multi-aura: kisa surede cok farkli hedef
        long window = (long) cfg("multi-window-ms", 300);
        d.recentTargets.put(victim.getUniqueId(), now);
        d.recentTargets.values().removeIf(t -> now - t > window);
        if (d.recentTargets.size() >= (int) cfg("multi-targets", 3)) {
            flag(p, "multi", "targets=" + d.recentTargets.size() + " in " + window + "ms");
            d.recentTargets.clear();
            flagged = true;
        }
        return flagged;
    }
}
