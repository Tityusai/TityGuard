package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.RayTraceResult;

public class FastBreakCheck extends Check {

    public FastBreakCheck(TityGuard plugin) {
        super(plugin, "fastbreak");
    }

    /** @return true ise kirma iptal edilmeli */
    public boolean handle(BlockBreakEvent e, Player p, PlayerData d, long now) {
        if (!isEnabled()) return false;
        boolean flagged = false;
        Block b = e.getBlock();

        // 1) Nuker hizi
        d.breakTimes.addLast(now);
        while (!d.breakTimes.isEmpty() && now - d.breakTimes.peekFirst() > 1000) d.breakTimes.pollFirst();
        if (d.breakTimes.size() > cfg("max-breaks-per-sec", 14)) {
            flag(p, "nuker", "breaks/s=" + d.breakTimes.size());
            d.breakTimes.clear();
            flagged = true;
        }

        // 2) Baktigi blok ile kirdigi blok ayni mi?
        RayTraceResult r = p.rayTraceBlocks(6.5);
        Block looked = r == null ? null : r.getHitBlock();
        if (looked == null || !looked.equals(b)) {
            double thr = cfg("buffer", 3);
            if (buffer(d, "ray", 1.0) > thr) {
                flag(p, "invalid-break", "not looking at the broken block");
                setBuffer(d, "ray", thr / 2);
                flagged = true;
            }
        } else {
            buffer(d, "ray", -0.5);
        }

        // 3) Kirma suresi
        if (!d.instaBreak) {
            if (d.digBlock != null && d.digBlock.equals(b)) {
                float speed = b.getBreakSpeed(p);
                if (speed > 0) {
                    double expectedMs = Math.ceil(1.0 / speed) * 50.0;
                    long elapsed = now - d.lastDigStart;
                    if (elapsed + 100 < expectedMs * cfg("min-ratio", 0.6)) {
                        flag(p, "speed", String.format("took %dms, expected ~%.0fms", elapsed, expectedMs));
                        flagged = true;
                    }
                }
            }
        }
        d.digging = false;
        d.instaBreak = false;
        return flagged;
    }
}
