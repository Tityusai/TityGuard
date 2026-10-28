package dev.tityguard.check;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.entity.Player;

public class AutoClickerCheck extends Check {

    public AutoClickerCheck(TityGuard plugin) {
        super(plugin, "autoclicker");
    }

    public boolean handle(Player p, PlayerData d, long now) {
        if (!isEnabled()) return false;
        // blok kirarken / yerlestirirken / item kullanirken olan kol sallamalarini sayma
        if (d.digging && now - d.lastDigStart < 15000) return false;
        if (now - d.lastInteract < 250) return false;

        boolean flagged = false;

        // 1) CPS
        d.clicks.addLast(now);
        while (!d.clicks.isEmpty() && now - d.clicks.peekFirst() > 1000) d.clicks.pollFirst();
        if (d.clicks.size() > cfg("max-cps", 22)) {
            flag(p, "cps", "cps=" + d.clicks.size());
            d.clicks.clear();
            flagged = true;
        }

        // 2) Aralik tutarliligi (standart sapma)
        int sample = (int) cfg("sample", 30);
        if (d.lastClick != 0) {
            long iv = now - d.lastClick;
            if (iv < 1000) {
                d.intervals.addLast(iv);
                while (d.intervals.size() > sample) d.intervals.pollFirst();
            } else {
                d.intervals.clear();
            }
        }
        d.lastClick = now;

        if (d.intervals.size() >= sample) {
            double mean = d.intervals.stream().mapToLong(Long::longValue).average().orElse(0);
            double var = d.intervals.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0);
            double std = Math.sqrt(var);
            if (mean < cfg("mean-max-ms", 300) && std < cfg("min-std-ms", 7)) {
                flag(p, "consistency", String.format("mean=%.1fms std=%.2fms", mean, std));
                d.intervals.clear();
                flagged = true;
            }
        }
        return flagged;
    }
}
