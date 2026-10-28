package dev.tityguard.listener;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class ConnectionListener implements Listener {

    private final TityGuard plugin;

    public ConnectionListener(TityGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.data(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.removeData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastTeleport = System.currentTimeMillis();
        d.resetMovement();
        d.lastMoveNanos = 0;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastTeleport = System.currentTimeMillis();
        d.resetMovement();
        d.lastMoveNanos = 0;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastTeleport = System.currentTimeMillis();
        d.resetMovement();
        d.lastMoveNanos = 0;
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        plugin.data(e.getPlayer()).lastVelocity = System.currentTimeMillis();
    }

    /** Iptal edilmis olsa bile FALL hasar eventi geldiyse NoFall bekleyen kaydi silinir. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        PlayerData d = plugin.data(p);
        d.lastDamage = System.currentTimeMillis();
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) d.pendingFall = 0;
    }
}
