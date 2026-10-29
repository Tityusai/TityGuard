package dev.tityguard.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import dev.tityguard.TityGuard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Netty thread'inde calisir: sadece veriyi okuyup ana thread'e aktarir. */
public class TityPacketListener extends PacketListenerAbstract {

    private final TityGuard plugin;

    public TityPacketListener(TityGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_VELOCITY) return;
        if (event.getUser() == null) return;
        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
        final int entityId = wrapper.getEntityId();
        var v = wrapper.getVelocity();
        final double vx = v.getX(), vy = v.getY(), vz = v.getZ();

        if (!plugin.isEnabled()) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || p.getEntityId() != entityId) return;
            plugin.checks().velocity.onVelocity(p, plugin.data(p), vx, vy, vz);
        });
    }
}
