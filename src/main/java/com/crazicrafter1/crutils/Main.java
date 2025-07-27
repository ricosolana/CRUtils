package com.crazicrafter1.crutils;

import com.crazicrafter1.crutils.ui.AbstractMenu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public boolean supportPlaceholders;

    public static Notifier notifier;

    // config
    public boolean debug = false;

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
        debug = config.getBoolean("debug", false);

        notifier = new Notifier(ChatColor.WHITE + "[%sCRUtils" + ChatColor.WHITE + "] %s%s", null, debug);
        notifier.debug("Debug is enabled");

        if (update) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "CRUtils", "CRUtils.jar", outTag)) {
                    notifier.warn("Updated to " + outTag);
                } else {
                    notifier.info("Using the latest version");
                }
            } catch (Exception e) {
                notifier.warn("Error while updating");
                e.printStackTrace();
            }
        else {
            notifier.warn("Updating is disabled");
            GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "CRUtils", (result, tag) -> { if (result) getLogger().warning("Update " + tag + " is available"); else getLogger().info("Using latest version"); });
        }

        supportPlaceholders = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        ConfigurationSerialization.registerClass(ItemBuilder.class, "ItemBuilder");

        new EventListener(this);
        new com.crazicrafter1.crutils.ui.EventListener(this);
    }

    @Override
    public void onDisable() {
        AbstractMenu.closeAllMenus();
    }

}
