package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;

public class TimerCheck extends Check {

    public TimerCheck(TityGuard plugin) {
        super(plugin, "timer");
    }

    /** Balance yontemi: her hareket paketi 50ms "butce" tuketir. Hizli gelirse birikim artar. */
    public boolean handle(Player p, PlayerData d, long now) {
        if (!isEnabled()) return false;

        long t = System.nanoTime();
        if (d.lastMoveNanos == 0 || plugin.recentLag(now, 3000)) {
            d.lastMoveNanos = t;
            d.timerBalance = 0;
            return false;
        }
        double delta = (t - d.lastMoveNanos) / 1_000_000.0;
        d.lastMoveNanos = t;

        d.timerBalance += 50.0 - delta;
        d.timerBalance = Math.max(d.timerBalance, -cfg("max-negative-ms", 200));

        if (d.timerBalance > cfg("threshold-ms", 200)) {
            double bal = d.timerBalance;
            d.timerBalance = 0;
            double v = buffer(d, "balance", 1.0);
            if (v > cfg("buffer", 2)) {
                flag(p, "balance", String.format("balance=%.0fms", bal));
                setBuffer(d, "balance", 0);
                return true;
            }
        } else {
            buffer(d, "balance", -0.02);
        }
        return false;
    }
}
