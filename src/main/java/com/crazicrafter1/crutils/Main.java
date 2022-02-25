package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    public boolean supportPlaceholders;

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        File noUpdateFile = new File(getDataFolder(), "NO_UPDATE.txt");
        if (!(noUpdateFile.exists() && noUpdateFile.isFile())) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar", outTag)) {
                    getLogger().warning("Updated to " + outTag + "; restart server to use");

                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
            } catch (Exception e) {
                getLogger().warning("Error while updating");
                e.printStackTrace();
            }
        else getLogger().warning("Updating is disabled (delete " + noUpdateFile.getName() + " to enable)");

        Main.instance = this;

        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        new EventListener(this);
    }
}
