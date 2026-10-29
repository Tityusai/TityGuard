package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import dev.tityguard.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * AntiKB / Velocity: sunucu oyuncuya velocity paketi yollar (knockback, patlama vb.).
 * Paket gonderildikten sonra bir sure boyunca oyuncunun gercekten hareket edip etmedigini olcer.
 * Duvar/tavan engeli varsa ilgili eksen degerlendirilmez.
 */
public class VelocityCheck extends Check {

    public VelocityCheck(TityGuard plugin) {
        super(plugin, "velocity");
    }

    /** Ana thread'de, velocity paketi gonderildiginde cagrilir. */
    public void onVelocity(Player p, PlayerData d, double vx, double vy, double vz) {
        if (!isEnabled()) return;
        if (d.isExemptCombat(p) || p.getAllowFlight() || p.isFlying() || p.isInsideVehicle()
                || p.isGliding() || p.isRiptiding() || p.isSleeping()) return;

        double h = Math.hypot(vx, vz);
        if (h < cfg("min-horizontal", 0.1) && vy < cfg("min-vertical", 0.2)) return;

        Location loc = p.getLocation();
        if (BlockUtil.inSpecial(loc)) return;

        long now = System.currentTimeMillis();
        d.velId++;
        final long id = d.velId;
        d.velPending = true;
        d.velH = h;
        d.velY = vy;
        d.accH = 0;
        d.maxDy = 0;
        d.velStart = now;
        d.velBlockedH = BlockUtil.blockedAhead(loc, vx, vz);
        d.velBlockedY = BlockUtil.ceilingAbove(loc);

        long delay = Math.max(0, p.getPing()) / 50 + (long) cfg("window-ticks", 6) + 1;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> evaluate(p, d, id), delay);
    }

    /** Her hareket event'inde (muafiyetten once) cagrilir. */
    public void onMove(PlayerData d) {
        if (!d.velPending) return;
        d.accH += d.deltaXZ;
        if (d.deltaY > d.maxDy) d.maxDy = d.deltaY;
    }

    private void evaluate(Player p, PlayerData d, long id) {
        if (!p.isOnline() || !d.velPending || d.velId != id) return;
        d.velPending = false;

        long now = System.currentTimeMillis();
        if (d.isExemptCombat(p) || p.getAllowFlight() || p.isInsideVehicle() || p.isGliding()) return;
        if (d.lastTeleport > d.velStart || d.lastSetback > d.velStart) return;
        if (plugin.recentLag(now, 2000)) return;
        if (BlockUtil.inSpecial(p.getLocation())) return;

        double ratio = cfg("min-ratio", 0.4);
        double minH = cfg("min-horizontal", 0.1);
        double minY = cfg("min-vertical", 0.2);

        boolean checkedH = !d.velBlockedH && d.velH >= minH;
        boolean checkedY = !d.velBlockedY && d.velY >= minY;
        if (!checkedH && !checkedY) return;

        boolean badH = checkedH && d.accH < d.velH * ratio;
        boolean badY = checkedY && d.maxDy < d.velY * ratio;

        double thr = cfg("buffer", 2);
        if (badH || badY) {
            if (buffer(d, "kb", 1.0) > thr) {
                String info = String.format("H: moved %.2f / expected %.2f%s | V: max %.2f / expected %.2f%s | ping=%d",
                        d.accH, d.velH, badH ? " (BAD)" : "",
                        d.maxDy, d.velY, badY ? " (BAD)" : "", p.getPing());
                flag(p, "antikb", info);
                setBuffer(d, "kb", thr / 2);
            }
        } else {
            buffer(d, "kb", -1.0);
        }
    }
}
