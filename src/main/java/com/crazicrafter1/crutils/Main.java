package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public boolean supportPlaceholders;

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        try {
            GitUtils.updatePlugin(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar", null);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error while updating CRUtils");
            e.printStackTrace();
        }
        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        new EventListener(this);
    }
}
