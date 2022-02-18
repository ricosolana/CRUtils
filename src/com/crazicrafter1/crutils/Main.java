package com.crazicrafter1.crutils;

import com.crazicrafter1.innerutils.GithubUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Main extends JavaPlugin {

    public final String prefix = Util.format("&8[&f&lCRUtils&r&8] ");

    public boolean debug;
    public boolean update;

    public boolean supportPlaceholders;

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        GithubUpdater.autoUpdate(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar");

        this.saveDefaultConfig();
        debug = this.getConfig().getBoolean("debug");
        update = this.getConfig().getBoolean("update");

        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        new EventListener(this);

        //new Cmd(this);
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
