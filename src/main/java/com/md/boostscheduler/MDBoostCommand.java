package com.md.boostscheduler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MDBoostCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("start", "stop", "status", "reload");

    private final MDBoostScheduler plugin;

    public MDBoostCommand(MDBoostScheduler plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (plugin.isRunning()) {
                    sender.sendMessage(prefix() + ChatColor.YELLOW + "El ciclo de boosters ya esta corriendo.");
                    return true;
                }
                plugin.startAll();
                sender.sendMessage(prefix() + ChatColor.GREEN + "Ciclo de boosters iniciado.");
            }
            case "stop" -> {
                if (!plugin.isRunning()) {
                    sender.sendMessage(prefix() + ChatColor.YELLOW + "El ciclo de boosters ya estaba detenido.");
                    return true;
                }
                plugin.stopAll();
                sender.sendMessage(prefix() + ChatColor.RED + "Ciclo de boosters detenido.");
            }
            case "reload" -> {
                plugin.loadSchedules();
                sender.sendMessage(prefix() + ChatColor.GREEN + "config.yml recargado.");
            }
            case "status" -> sendStatus(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage(prefix() + (plugin.isRunning()
                ? ChatColor.GREEN + "Estado: CORRIENDO"
                : ChatColor.RED + "Estado: DETENIDO"));

        List<BoosterSchedule> schedules = plugin.getSchedules();
        if (schedules.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No hay boosters configurados.");
            return;
        }

        for (BoosterSchedule s : schedules) {
            String estado = s.isEnabled() ? ChatColor.GREEN + "activado" : ChatColor.GRAY + "desactivado";
            sender.sendMessage(ChatColor.AQUA + " - " + s.getId() + ChatColor.WHITE
                    + " | tipo: " + s.getBoosterType()
                    + " | +" + s.getMultiplier() + "%"
                    + " | duracion: " + s.getDurationMinutes() + "m"
                    + " | cada: " + s.getIntervalMinutes() + "m"
                    + " | " + estado);

            if (plugin.isRunning() && s.isEnabled() && s.getNextTriggerEpochMillis() > 0) {
                long secondsLeft = Math.max(0, (s.getNextTriggerEpochMillis() - System.currentTimeMillis()) / 1000);
                sender.sendMessage(ChatColor.GRAY + "   proxima activacion en: " + formatSeconds(secondsLeft));
            }
        }
    }

    private String formatSeconds(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(prefix() + ChatColor.WHITE + "Uso: /mdboost <start|stop|status|reload>");
    }

    private String prefix() {
        return ChatColor.AQUA + "[MDBoost] " + ChatColor.RESET;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) out.add(sub);
            }
            return out;
        }
        return List.of();
    }
}
