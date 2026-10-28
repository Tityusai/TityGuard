package dev.tityguard.listener;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldListener implements Listener {

    private final TityGuard plugin;

    public WorldListener(TityGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a == Action.LEFT_CLICK_BLOCK || a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR) {
            plugin.data(e.getPlayer()).lastInteract = System.currentTimeMillis();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageBlock(BlockDamageEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.digging = true;
        d.lastDigStart = System.currentTimeMillis();
        d.digBlock = e.getBlock();
        d.instaBreak = e.getInstaBreak();
    }

    @EventHandler
    public void onDamageAbort(BlockDamageAbortEvent e) {
        plugin.data(e.getPlayer()).digging = false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        PlayerData d = plugin.data(p);
        if (d.isExemptCombat(p)) return;
        if (plugin.checks().fastBreak.handle(e, p, d, System.currentTimeMillis())
                && plugin.getConfig().getBoolean("settings.cancel-hits", true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        PlayerData d = plugin.data(p);
        if (d.isExemptCombat(p)) return;
        if (plugin.checks().scaffold.handle(e, p, d, System.currentTimeMillis())
                && plugin.getConfig().getBoolean("settings.cancel-hits", true)) {
            e.setCancelled(true);
        }
    }
}
