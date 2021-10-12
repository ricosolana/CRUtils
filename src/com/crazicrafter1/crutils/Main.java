package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Main extends JavaPlugin {

    public final String prefix = ChatColor.translateAlternateColorCodes('&',
            "&8[&f&lCRUtils&r&8] ");

    public static boolean debug;

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        //if (ReflectionUtil.isVersion("1_17_1")) {
        //    error("Works only on 1.17.1 (latest minecraft version)");
        //    Bukkit.getPluginManager().disablePlugin(this);
        //    return;
        //}

        this.saveDefaultConfig();
        debug = this.getConfig().getBoolean("debug");

        // register checker
        new Updater(this, "PeriodicSeizures", "CRUtils", false);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Updater> entry : Updater.updates.entrySet()) {
                    entry.getValue().updateFromGithub();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 1, 3600 * 24 * 20);

        new EventListener(this);
    }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_GRAY + s);
    }

    public void important(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.LIGHT_PURPLE + s);
    }

    public void warn(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + s);
    }

    public void error(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + s);
    }

    public void debug(String s) {
        if (debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + s);
    }
}
