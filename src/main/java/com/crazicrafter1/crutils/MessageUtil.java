package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import javax.annotation.Nonnull;

public class MessageUtil {
    //CONSOLE,    // send message to console
    //GLOBAL,  // broadcast to all online players once
    //OFFLINE,    // broadcast to players who join
    //CONSOLE_GLOBAL,
    //CONSOLE_OFFLINE,
    //ALL
    //;

    private final String prefix;

    public MessageUtil(String prefix) {
        this.prefix = prefix;
    }

    private String format(ChatColor color, String message, ChatColor messageColor) {
        return ChatColor.WHITE + "[" + color + prefix + ChatColor.WHITE + "]" + ChatColor.RESET + messageColor + message;
    }

    public void info(String s) {
        info(Bukkit.getConsoleSender(), s);
    }

    public void warn(String s) {
        warn(Bukkit.getConsoleSender(), s);
    }

    public void severe(String s) {
        severe(Bukkit.getConsoleSender(), s);
    }

    public void info(@Nonnull CommandSender sender, String s) {
        sender.sendMessage(format(ChatColor.BLUE, s, ChatColor.RESET));
    }

    public void warn(@Nonnull CommandSender sender, String s) {
        sender.sendMessage(format(ChatColor.YELLOW, s, ChatColor.YELLOW));
    }

    public void severe(@Nonnull CommandSender sender, String s) {
        sender.sendMessage(format(ChatColor.RED, s, ChatColor.RED));
    }

    public boolean commandInfo(@Nonnull CommandSender sender, @Nonnull String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.BLUE + prefix + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                        + ChatColor.RESET + s);
        return true;
    }

    public boolean commandWarn(@Nonnull CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.YELLOW + prefix + ChatColor.WHITE + "]" : "" + ChatColor.GOLD + ChatColor.BOLD + "\u26A1") + " "
                        + ChatColor.RESET + ChatColor.YELLOW + s);
        return true;
    }

    public boolean commandSevere(@Nonnull CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.RED + prefix + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                        + ChatColor.RESET + ChatColor.RED + s);
        return true;
    }
}
