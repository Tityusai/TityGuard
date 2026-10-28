package dev.tityguard.listener;

import dev.tityguard.TityGuard;
import dev.tityguard.check.CheckManager;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

public class CombatListener implements Listener {

    private final TityGuard plugin;

    public CombatListener(TityGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        PlayerData d = plugin.data(p);
        if (d.isExemptCombat(p)) return;

        CheckManager checks = plugin.checks();
        long now = System.currentTimeMillis();

        boolean cancel = false;
        cancel |= checks.reach.handle(p, d, victim);
        cancel |= checks.killAura.handle(p, d, victim, now);

        if (cancel && plugin.getConfig().getBoolean("settings.cancel-hits", true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player p = e.getPlayer();
        PlayerData d = plugin.data(p);
        if (d.isExemptCombat(p)) return;
        plugin.checks().autoClicker.handle(p, d, System.currentTimeMillis());
    }
}
