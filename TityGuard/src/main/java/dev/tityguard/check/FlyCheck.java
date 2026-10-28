package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class FlyCheck extends Check {

    public FlyCheck(TityGuard plugin) {
        super(plugin, "fly");
    }

    /** Yercekimi tahmini: vy(n+1) = (vy(n) - 0.08) * 0.98 */
    public boolean handle(Player p, PlayerData d) {
        if (!isEnabled()) return false;

        double dy = d.deltaY;

        if (d.onGround) {
            d.airTicks = 0;
            d.lastDy = dy;
            buffer(d, "gravity", -0.5);
            return false;
        }
        d.airTicks++;

        if (d.touching || d.inSpecial
                || p.hasPotionEffect(PotionEffectType.LEVITATION)
                || p.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            d.lastDy = null;
            return false;
        }

        if (d.airTicks < 3 || d.lastDy == null) {
            d.lastDy = dy;
            return false;
        }

        double last = Math.abs(d.lastDy) < 0.003 ? 0.0 : d.lastDy;
        double predicted = (last - 0.08) * 0.98;
        double diff = Math.abs(dy - predicted);
        d.lastDy = dy;

        if (diff > cfg("tolerance", 0.03)) {
            double v = buffer(d, "gravity", 1.0);
            if (v > cfg("buffer", 4)) {
                flag(p, "gravity", String.format("dy=%.4f expected=%.4f air=%d", dy, predicted, d.airTicks));
                setBuffer(d, "gravity", cfg("buffer", 4) / 2);
                return true;
            }
        } else {
            buffer(d, "gravity", -0.25);
        }
        return false;
    }
}
