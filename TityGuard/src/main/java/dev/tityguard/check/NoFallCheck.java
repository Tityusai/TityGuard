package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.GameRule;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NoFallCheck extends Check {

    public NoFallCheck(TityGuard plugin) {
        super(plugin, "nofall");
    }

    /**
     * Sunucu tarafinda dusme mesafesini kendimiz hesaplariz. Yeterince yuksekten
     * inip yere carptiysa ve kisa sure icinde FALL hasari gelmediyse flag atilir.
     */
    public void handle(Player p, PlayerData d, long now) {
        if (!isEnabled()) return;

        if (d.inSpecial) {
            d.fallDist = 0;
            return;
        }
        if (d.deltaY < 0) d.fallDist += -d.deltaY;

        if (!d.onGround) return;

        double fall = d.fallDist;
        d.fallDist = 0;

        int ff = 0;
        ItemStack boots = p.getInventory().getBoots();
        if (boots != null) ff = boots.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
        double threshold = cfg("min-fall", 6.0) + ff * 2.0;
        if (fall < threshold) return;

        PotionEffect res = p.getPotionEffect(PotionEffectType.RESISTANCE);
        if (p.isInvulnerable()
                || p.hasPotionEffect(PotionEffectType.SLOW_FALLING)
                || (res != null && res.getAmplifier() >= 4)
                || now - d.lastDamage < 600
                || Boolean.FALSE.equals(p.getWorld().getGameRuleValue(GameRule.FALL_DAMAGE))) {
            return;
        }

        d.pendingFall = fall;
        d.pendingAt = now;
        final long stamp = now;
        final double dist = fall;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;
            if (d.pendingFall > 0 && d.pendingAt == stamp) {
                d.pendingFall = 0;
                flag(p, "ground", String.format("fell %.1f blocks, no damage", dist));
            }
        }, 4L);
    }
}
