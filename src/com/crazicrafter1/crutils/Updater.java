package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static com.crazicrafter1.crutils.Util.copy;

public class Updater {

    static ConcurrentHashMap<String, Updater> updates = new ConcurrentHashMap<>();

    private final String pluginName;
    private final String githubAuthor;
    private final String githubProject;
    private final boolean doUpdates;
    private String latestVersion;
    private URL latestdownloadUrl;

    public Updater(Plugin plugin, String githubAuthor, String githubProject, boolean doUpdates) {
        this.pluginName = plugin.getName();
        this.githubAuthor = githubAuthor;
        this.githubProject = githubProject;
        this.doUpdates = doUpdates;

        updates.put(pluginName, this);
    }

    Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }

    String getCurrentVersion() {
        return getPlugin().getDescription().getVersion();
    }

    boolean isOutdated() throws IOException {
        checkVersions();
        return Integer.parseInt(getCurrentVersion().replaceAll("\\.", "")) <
                Integer.parseInt(latestVersion.replaceAll("\\.", ""));
    }

    void checkVersions() throws IOException {
        URLConnection con = new URL(
                String.format("https://github.com/%s/%s/releases/latest/", githubAuthor, githubProject)).openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        con.connect();

        InputStream is = con.getInputStream();
        URL download = con.getURL();
        is.close();

        latestVersion = download.toString().substring(download.toString().lastIndexOf('/') + 1);
        latestdownloadUrl = new URL(download.toString() + "/" + pluginName + ".jar");

        //Main.getInstance().debug(download.toString());
        //Main.getInstance().debug(latestVersion);
        //Main.getInstance().debug(latestdownloadUrl.toString());
    }

    void updateFromGithub() {
        try {
            if (!isOutdated()) {
                Main.getInstance().info(pluginName + " is-up-to date");
                return;
            }

            if (!doUpdates) {
                Main.getInstance().important("A new update is available for " + pluginName + " (" + latestVersion + "), please consider installing it!");
                return;
            }

            Main.getInstance().important("Updating " + pluginName + " to " + latestVersion);

            Plugin plugin = getPlugin();

            Bukkit.getPluginManager().disablePlugin(plugin);

            /*
             * Make a backup of the plugin
             */
            File pluginFile = new File(URLDecoder.decode(
                    plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                    StandardCharsets.UTF_8));

            // Copy plugin contents to back up
            File backupFile = new File(plugin.getName() + "-backup.jar");
            copy(new FileInputStream(pluginFile), new FileOutputStream(backupFile));

            // Delete the old plugin
            pluginFile.setWritable(true, false);
            pluginFile.delete();

            /*
             * Start download
             */
            InputStream in = latestdownloadUrl.openStream();
            copy(in, new FileOutputStream(pluginFile));

            if (pluginFile.length() < 1000) {
                // Fallback contingencies
                copy(new FileInputStream(backupFile),
                        new FileOutputStream(pluginFile));
            } else {
                // All is fine, delete the backup
                backupFile.delete();
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginFile));
                    } catch (InvalidPluginException | InvalidDescriptionException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(Main.getInstance(), 3 * 20);

        } catch (NumberFormatException e) {
            Main.getInstance().error("There are no Github releases for " + pluginName);
        } catch (Exception e) {
            Main.getInstance().error("Couldn't update " + pluginName);
            if (Main.debug)
                e.printStackTrace();
        }
    }
}
