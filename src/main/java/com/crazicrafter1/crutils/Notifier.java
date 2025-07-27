package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.StandardMessenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Notifier {
    //CONSOLE,    // send message to console
    //GLOBAL,  // broadcast to all online players once
    //OFFLINE,    // broadcast to players who join
    //CONSOLE_GLOBAL,
    //CONSOLE_OFFLINE,
    //ALL
    //;

    private static final String SYM_INFO = "\u24D8";
    private static final String SYM_WARN = "\u26A1";
    private static final String SYM_SEVERE = "\u26A0";

    private final String format;
    private final String perm;
    private final boolean debug;

    //private static class NotifyListener implements Listener {
    //    public ArrayList<String> messages = new ArrayList<>();
    //    public HashSet<UUID> notified = new HashSet<>();
    //
    //    public NotifyListener(JavaPlugin plugin) {
    //        Bukkit.getPluginManager().registerEvents(this, plugin);
    //    }
    //    @EventHandler
    //    private void onJoin(PlayerJoinEvent event) {
    //        Player p = event.getPlayer();
    //        if (!notified.contains(p.getUniqueId())) {
    //            notified.add(p.getUniqueId());
    //            p.sendMessage();
    //        }
    //    }
    //}

    public Notifier(@Nonnull String format, @Nullable String perm, boolean debug) {
        this.format = format;
        this.perm = perm;
        this.debug = debug;
    }

    public Notifier(@Nonnull String format, @Nullable String perm) {
        this(format, perm, false);
    }

    public Notifier(@Nonnull String format) {
        this(format, null);
    }

    private void send(@Nonnull CommandSender sender, @Nonnull String message, @Nonnull String prefixColor, @Nonnull String messageColor) {
        //sender.sendMessage(Arrays.stream(message.split("\n")).foString.format(format, prefixColor, messageColor, message));
        sender.sendMessage(String.format(format, prefixColor, messageColor, message));
    }

    private void sendCommand(@Nonnull CommandSender sender, @Nonnull String message, @Nonnull String symbol) {
        sender.sendMessage("" + ChatColor.BOLD + ChatColor.DARK_GRAY + symbol + " " + ChatColor.RESET + ChatColor.GRAY + message);
    }



    public void debug(@Nonnull String message) {
        debug(Bukkit.getConsoleSender(), message);
    }

    public void info(@Nonnull String message) {
        info(Bukkit.getConsoleSender(), message);
    }

    public void warn(@Nonnull String message) {
        warn(Bukkit.getConsoleSender(), message);
    }

    public void severe(@Nonnull String message) {
        severe(Bukkit.getConsoleSender(), message);
    }



    public void debug(@Nonnull CommandSender sender, @Nonnull String message) {
        if (debug) {
            send(sender, message, ChatColor.GOLD.toString(), ChatColor.YELLOW.toString());
        }
    }

    public void info(@Nonnull CommandSender sender, @Nonnull String message) {
        send(sender, message, ChatColor.BLUE.toString(), ChatColor.GRAY.toString());
    }

    public void warn(@Nonnull CommandSender sender, @Nonnull String message) {
        send(sender, message, ChatColor.YELLOW.toString(), ChatColor.YELLOW.toString());
    }

    public void severe(@Nonnull CommandSender sender, @Nonnull String message) {
        send(sender, message, ChatColor.RED.toString(), ChatColor.RED.toString());
    }



    public boolean commandInfo(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof ConsoleCommandSender)
            info(sender, message);
        else
            sendCommand(sender, message, SYM_INFO);
        return true;
    }

    public boolean commandWarn(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof ConsoleCommandSender)
            warn(sender, message);
        else
            sendCommand(sender, message, SYM_WARN);
        return true;
    }

    public boolean commandSevere(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof ConsoleCommandSender)
            severe(sender, message);
        else
            sendCommand(sender, message, SYM_SEVERE);
        return true;
    }

    public void warnAll(@Nonnull String message) {
        warn(message);
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (perm != null) {
                if (p.hasPermission(perm)) {
                    warn(p, message);
                }
            } else {
                if (p.isOp()) {
                    warn(p, message);
                }
            }
        });
    }

    @Deprecated
    public void globalWarn(@Nonnull String message) {
        warnAll(message);
    }





    //public void severeGlobal(@Nonnull CommandSender sender, String message) {
    //    if (joinListener == null) {
    //        joinListener = new Listener() {

    //        }
    //    }
    //}

}
