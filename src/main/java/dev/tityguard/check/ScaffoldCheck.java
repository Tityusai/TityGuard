package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.RayTraceResult;

public class ScaffoldCheck extends Check {

    public ScaffoldCheck(TityGuard plugin) {
        super(plugin, "scaffold");
    }

    /** @return true ise yerlestirme iptal edilmeli */
    public boolean handle(BlockPlaceEvent e, Player p, PlayerData d, long now) {
        if (!isEnabled()) return false;
        boolean flagged = false;

        // 1) FastPlace
        d.placeTimes.addLast(now);
        while (!d.placeTimes.isEmpty() && now - d.placeTimes.peekFirst() > 1000) d.placeTimes.pollFirst();
        if (d.placeTimes.size() > cfg("max-place-per-sec", 15)) {
            flag(p, "fastplace", "blocks/s=" + d.placeTimes.size());
            d.placeTimes.clear();
            flagged = true;
        }

        // 2) Gecersiz yerlestirme: crosshair'in baktigi blok ile "yaslanilan" blok ayni mi?
        Block against = e.getBlockAgainst();
        if (against.isLiquid() || e.getBlockPlaced().getType() == Material.LILY_PAD) return flagged;

        RayTraceResult r = p.rayTraceBlocks(cfg("reach", 6.5));
        Block hit = r == null ? null : r.getHitBlock();
        if (hit == null || !hit.equals(against)) {
            double thr = cfg("buffer", 4);
            if (buffer(d, "ray", 1.0) > thr) {
                flag(p, "invalid-place", "not looking at the block it was placed against");
                setBuffer(d, "ray", thr / 2);
                flagged = true;
            }
        } else {
            buffer(d, "ray", -0.5);
        }
        return flagged;
    }
}
