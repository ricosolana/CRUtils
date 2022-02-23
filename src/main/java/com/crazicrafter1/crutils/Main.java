package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public final String prefix = ColorUtil.color("&8[&f&lCRUtils&r&8] ");

    public boolean supportPlaceholders;

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        GithubUpdater.autoUpdate(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar");

        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        new EventListener(this);
    }
}
