package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import dev.tityguard.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends Check {

    public SpeedCheck(TityGuard plugin) {
        super(plugin, "speed");
    }

    /** @return true ise flag atildi (setback istenebilir) */
    public boolean handle(Player p, PlayerData d, Location to, long now) {
        if (!isEnabled()) return false;
        if (d.inSpecial || p.isSwimming() || now - d.lastSlippery < 1500) {
            buffer(d, "limit", -1);
            return false;
        }

        double mult = p.getWalkSpeed() / 0.2;
        PotionEffect sp = p.getPotionEffect(PotionEffectType.SPEED);
        if (sp != null) mult *= 1.0 + 0.2 * (sp.getAmplifier() + 1);

        double limit = (d.onGround ? cfg("ground-limit", 0.38) : cfg("air-limit", 0.42)) * mult;
        if (BlockUtil.headObstructed(to)) limit *= 1.6;
        limit += cfg("tolerance", 0.02);

        if (d.deltaXZ > limit) {
            double v = buffer(d, "limit", 1.0);
            if (v > cfg("buffer", 5)) {
                flag(p, "limit", String.format("dist=%.3f limit=%.3f ground=%s", d.deltaXZ, limit, d.onGround));
                setBuffer(d, "limit", cfg("buffer", 5) / 2);
                return true;
            }
        } else {
            buffer(d, "limit", -0.5);
        }
        return false;
    }
}
