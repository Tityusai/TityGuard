package dev.tityguard.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class BlockUtil {

    private BlockUtil() {}

    private static int floor(double v) {
        return (int) Math.floor(v);
    }

    private static Block safeBlock(World w, int x, int y, int z) {
        if (y < w.getMinHeight() || y >= w.getMaxHeight()) return null;
        if (!w.isChunkLoaded(x >> 4, z >> 4)) return null;
        return w.getBlockAt(x, y, z);
    }

    /** Sunucu tarafi zemin tespiti (client'in onGround'una guvenmiyoruz). */
    public static boolean isOnGround(Location l) {
        World w = l.getWorld();
        double px = l.getX(), py = l.getY(), pz = l.getZ();
        for (int x = floor(px - 0.3); x <= floor(px + 0.3); x++) {
            for (int z = floor(pz - 0.3); z <= floor(pz + 0.3); z++) {
                for (int y = floor(py) - 1; y <= floor(py); y++) {
                    Block b = safeBlock(w, x, y, z);
                    if (b == null || b.isPassable()) continue;
                    for (BoundingBox bb : b.getCollisionShape().getBoundingBoxes()) {
                        double diff = py - (y + bb.getMaxY());
                        if (diff >= -0.02 && diff <= 0.03
                                && x + bb.getMinX() < px + 0.3 && x + bb.getMaxX() > px - 0.3
                                && z + bb.getMinZ() < pz + 0.3 && z + bb.getMaxZ() > pz - 0.3) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Etrafta (ayak alti, bas ustu, yanlar) herhangi bir kati blok var mi? */
    public static boolean touchingBlocks(Location l) {
        World w = l.getWorld();
        double px = l.getX(), py = l.getY(), pz = l.getZ();
        for (int x = floor(px - 0.35); x <= floor(px + 0.35); x++) {
            for (int z = floor(pz - 0.35); z <= floor(pz + 0.35); z++) {
                for (int y = floor(py - 0.5); y <= floor(py + 2.1); y++) {
                    Block b = safeBlock(w, x, y, z);
                    if (b != null && !b.isPassable()) return true;
                }
            }
        }
        return false;
    }

    public static boolean headObstructed(Location l) {
        World w = l.getWorld();
        double px = l.getX(), py = l.getY(), pz = l.getZ();
        for (int x = floor(px - 0.3); x <= floor(px + 0.3); x++) {
            for (int z = floor(pz - 0.3); z <= floor(pz + 0.3); z++) {
                Block b = safeBlock(w, x, floor(py + 1.9), z);
                if (b != null && !b.isPassable()) return true;
            }
        }
        return false;
    }

    private static boolean special(Block b) {
        if (b == null) return false;
        if (b.isLiquid()) return true;
        Material m = b.getType();
        if (Tag.CLIMBABLE.isTagged(m)) return true;
        if (m == Material.COBWEB || m == Material.SWEET_BERRY_BUSH || m == Material.POWDER_SNOW
                || m == Material.SCAFFOLDING || m == Material.BUBBLE_COLUMN
                || m == Material.HONEY_BLOCK || m == Material.SLIME_BLOCK
                || m == Material.SOUL_SAND || m.name().endsWith("_BED")) return true;
        return b.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged();
    }

    /** Su, orumcek agi, merdiven, slime, yatak vb. fizigi bozan bloklar. */
    public static boolean inSpecial(Location l) {
        World w = l.getWorld();
        double px = l.getX(), py = l.getY(), pz = l.getZ();
        int[] ys = {floor(py - 0.2), floor(py), floor(py + 1.0)};
        double[] offs = {-0.3, 0.0, 0.3};
        for (double ox : offs) {
            for (double oz : offs) {
                for (int y : ys) {
                    if (special(safeBlock(w, floor(px + ox), y, floor(pz + oz)))) return true;
                }
            }
        }
        return false;
    }

    public static boolean isSlippery(Location l) {
        World w = l.getWorld();
        for (int dy = 0; dy <= 1; dy++) {
            Block b = safeBlock(w, floor(l.getX()), floor(l.getY() - 0.5) - dy, floor(l.getZ()));
            if (b == null) continue;
            Material m = b.getType();
            if (m == Material.ICE || m == Material.PACKED_ICE || m == Material.BLUE_ICE
                    || m == Material.FROSTED_ICE) return true;
        }
        return false;
    }

    public static Vector nearestPoint(Vector p, BoundingBox bb) {
        return new Vector(
                Math.max(bb.getMinX(), Math.min(p.getX(), bb.getMaxX())),
                Math.max(bb.getMinY(), Math.min(p.getY(), bb.getMaxY())),
                Math.max(bb.getMinZ(), Math.min(p.getZ(), bb.getMaxZ())));
    }

    public static boolean canSee(Location eye, Vector target) {
        Vector dir = target.clone().subtract(eye.toVector());
        double len = dir.length();
        if (len < 0.1) return true;
        dir.normalize();
        RayTraceResult r = eye.getWorld().rayTraceBlocks(eye, dir, len, FluidCollisionMode.NEVER, true);
        return r == null || r.getHitBlock() == null;
    }
}
