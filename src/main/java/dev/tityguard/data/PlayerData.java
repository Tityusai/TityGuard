package dev.tityguard.data;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    public final UUID uuid;
    public final long joinTime = System.currentTimeMillis();
    public boolean alerts = true;

    // zaman damgalari
    public long lastTeleport, lastVelocity, lastDamage, lastSetback, lastSlippery, lastInteract;

    // hareket
    public double deltaXZ, deltaY;
    public boolean onGround, touching, inSpecial;
    public Double lastDy;
    public int airTicks;
    public double fallDist, pendingFall;
    public long pendingAt;
    public long lastMoveNanos;
    public double timerBalance;

    // velocity (anti-kb)
    public boolean velPending, velBlockedH, velBlockedY;
    public double velH, velY, accH, maxDy;
    public long velId, velStart;

    // savas
    public final Deque<Long> clicks = new ArrayDeque<>();
    public final Deque<Long> intervals = new ArrayDeque<>();
    public long lastClick;
    public final Map<UUID, Long> recentTargets = new HashMap<>();

    // dunya
    public boolean digging, instaBreak;
    public long lastDigStart;
    public Block digBlock;
    public final Deque<Long> placeTimes = new ArrayDeque<>();
    public final Deque<Long> breakTimes = new ArrayDeque<>();

    public final Map<String, Double> buffers = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void resetMovement() {
        lastDy = null;
        airTicks = 0;
        fallDist = 0;
        pendingFall = 0;
        timerBalance = 0;
    }

    public boolean isExemptCombat(Player p) {
        GameMode gm = p.getGameMode();
        return p.hasPermission("tityguard.bypass")
                || gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR
                || p.isDead();
    }

    public boolean isExempt(Player p, long now, long joinGrace) {
        return isExemptCombat(p)
                || p.getAllowFlight() || p.isFlying() || p.isGliding()
                || p.isInsideVehicle() || p.isRiptiding() || p.isSleeping()
                || now - joinTime < joinGrace
                || now - lastTeleport < 1000
                || now - lastSetback < 300
                || now - lastVelocity < 1500;
    }
}
