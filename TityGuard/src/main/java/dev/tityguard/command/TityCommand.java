package dev.tityguard.command;

import dev.tityguard.TityGuard;
import dev.tityguard.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TityCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Tity" + ChatColor.DARK_AQUA + "Guard" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

    private final TityGuard plugin;

    public TityCommand(TityGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        if (a.length == 0) {
            help(s);
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "alerts" -> {
                if (!(s instanceof Player p)) {
                    s.sendMessage(PREFIX + "Sadece oyuncular kullanabilir.");
                    return true;
                }
                if (!p.hasPermission("tityguard.alerts")) {
                    p.sendMessage(PREFIX + ChatColor.RED + "Yetkin yok.");
                    return true;
                }
                PlayerData d = plugin.data(p);
                d.alerts = !d.alerts;
                p.sendMessage(PREFIX + "Alert'ler " + (d.alerts ? ChatColor.GREEN + "acik" : ChatColor.RED + "kapali"));
            }
            case "reload" -> {
                if (!s.hasPermission("tityguard.admin")) {
                    s.sendMessage(PREFIX + ChatColor.RED + "Yetkin yok.");
                    return true;
                }
                plugin.reloadConfig();
                s.sendMessage(PREFIX + ChatColor.GREEN + "Config yenilendi.");
            }
            case "info" -> {
                if (a.length < 2) {
                    s.sendMessage(PREFIX + "Kullanim: /tg info <oyuncu>");
                    return true;
                }
                Player t = Bukkit.getPlayerExact(a[1]);
                if (t == null) {
                    s.sendMessage(PREFIX + ChatColor.RED + "Oyuncu bulunamadi.");
                    return true;
                }
                Map<String, Double> vls = plugin.violations().get(t.getUniqueId());
                s.sendMessage(PREFIX + t.getName() + " | ping " + t.getPing() + "ms");
                if (vls.isEmpty()) {
                    s.sendMessage(ChatColor.GREEN + "  Ihlal yok.");
                } else {
                    vls.forEach((k, v) -> s.sendMessage(ChatColor.GRAY + "  " + k + ": " + ChatColor.RED + String.format("%.0f", v)));
                }
            }
            case "reset" -> {
                if (!s.hasPermission("tityguard.admin")) {
                    s.sendMessage(PREFIX + ChatColor.RED + "Yetkin yok.");
                    return true;
                }
                if (a.length < 2) {
                    s.sendMessage(PREFIX + "Kullanim: /tg reset <oyuncu>");
                    return true;
                }
                Player t = Bukkit.getPlayerExact(a[1]);
                if (t == null) {
                    s.sendMessage(PREFIX + ChatColor.RED + "Oyuncu bulunamadi.");
                    return true;
                }
                plugin.violations().reset(t.getUniqueId());
                s.sendMessage(PREFIX + t.getName() + " VL'leri sifirlandi.");
            }
            default -> help(s);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage(PREFIX + "Komutlar:");
        s.sendMessage(ChatColor.GRAY + " /tg alerts " + ChatColor.DARK_GRAY + "- alert ac/kapat");
        s.sendMessage(ChatColor.GRAY + " /tg info <oyuncu> " + ChatColor.DARK_GRAY + "- VL goster");
        s.sendMessage(ChatColor.GRAY + " /tg reset <oyuncu> " + ChatColor.DARK_GRAY + "- VL sifirla");
        s.sendMessage(ChatColor.GRAY + " /tg reload " + ChatColor.DARK_GRAY + "- config yenile");
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            for (String o : List.of("alerts", "info", "reset", "reload")) {
                if (o.startsWith(a[0].toLowerCase())) out.add(o);
            }
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("info") || a[0].equalsIgnoreCase("reset"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(a[1].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
