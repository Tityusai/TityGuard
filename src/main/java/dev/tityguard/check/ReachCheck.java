package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import dev.tityguard.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ReachCheck extends Check {

    public ReachCheck(TityGuard plugin) {
        super(plugin, "reach");
    }

    public boolean handle(Player p, PlayerData d, LivingEntity victim) {
        if (!isEnabled()) return false;

        boolean flagged = false;
        Location eye = p.getEyeLocation();
        Vector near = BlockUtil.nearestPoint(eye.toVector(), victim.getBoundingBox());
        double dist = eye.toVector().distance(near);

        int ping = Math.min(p.getPing(), (int) cfg("max-ping", 250));
        double allowed = cfg("max-reach", 3.1) + ping * cfg("ping-comp", 0.002);

        if (dist > allowed) {
            double v = buffer(d, "dist", 1.0);
            if (v > cfg("buffer", 3)) {
                flag(p, "distance", String.format("reach=%.2f allowed=%.2f ping=%d", dist, allowed, p.getPing()));
                setBuffer(d, "dist", cfg("buffer", 3) / 2);
                flagged = true;
            }
        } else {
            buffer(d, "dist", -0.25);
        }

        // duvar arkasindan vurma
        if (dist > 0.5) {
            boolean visible = BlockUtil.canSee(eye, near)
                    || BlockUtil.canSee(eye, victim.getBoundingBox().getCenter())
                    || BlockUtil.canSee(eye, victim.getEyeLocation().toVector());
            if (!visible) {
                double v = buffer(d, "wall", 1.0);
                if (v > 2) {
                    flag(p, "wall", String.format("hit through blocks, dist=%.2f", dist));
                    setBuffer(d, "wall", 0);
                    flagged = true;
                }
            } else {
                buffer(d, "wall", -0.5);
            }
        }
        return flagged;
    }
}
