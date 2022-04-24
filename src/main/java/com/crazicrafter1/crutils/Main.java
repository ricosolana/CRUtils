package com.crazicrafter1.crutils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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
        Main.instance = this;

        saveDefaultConfig();
        FileConfiguration config = getConfig();
        boolean update = config.getBoolean("update", false);

        if (update) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar", outTag)) {
                    getLogger().warning("Updated to " + outTag);
                } else {
                    getLogger().info("Using the latest version");
                }
            } catch (Exception e) {
                getLogger().warning("Error while updating");
                e.printStackTrace();
            }
        else {
            getLogger().warning("Updating is disabled");
            GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "CRUtils", (result, tag) -> { if (result) getLogger().warning("Update " + tag + " is available"); else getLogger().info("Using latest version"); });
        }

        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        ConfigurationSerialization.registerClass(ItemBuilder.class, "ItemBuilder");

        new EventListener(this);
    }
}
