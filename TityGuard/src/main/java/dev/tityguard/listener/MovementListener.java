package dev.tityguard.listener;

import dev.tityguard.TityGuard;
import dev.tityguard.check.CheckManager;
import dev.tityguard.data.PlayerData;
import dev.tityguard.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    private final TityGuard plugin;

    public MovementListener(TityGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null || !from.getWorld().equals(to.getWorld())) return;

        Player p = e.getPlayer();
        PlayerData d = plugin.data(p);
        CheckManager checks = plugin.checks();
        long now = System.currentTimeMillis();

        long grace = plugin.getConfig().getLong("settings.join-grace-ms", 3000L);
        if (d.isExempt(p, now, grace)) {
            d.resetMovement();
            d.lastMoveNanos = 0;
            return;
        }

        boolean setback = false;

        if (checks.timer.handle(p, d, now)) setback |= checks.timer.setbackEnabled();

        boolean moved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        if (moved) {
            d.deltaXZ = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
            d.deltaY = to.getY() - from.getY();
            d.onGround = BlockUtil.isOnGround(to);
            d.touching = BlockUtil.touchingBlocks(to);
            d.inSpecial = BlockUtil.inSpecial(to);
            if (BlockUtil.isSlippery(to)) d.lastSlippery = now;

            if (checks.speed.handle(p, d, to, now)) setback |= checks.speed.setbackEnabled();
            if (checks.fly.handle(p, d)) setback |= checks.fly.setbackEnabled();
            checks.noFall.handle(p, d, now);
        }

        if (setback) {
            e.setCancelled(true);
            d.resetMovement();
            d.lastSetback = now;
            d.lastMoveNanos = 0;
        }
    }
}
