package com.crazicrafter1.crutils.DEPRECATED;

import com.crazicrafter1.crutils.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.crazicrafter1.crutils.Util.copy;

public class GithubUpdater {
    public static boolean autoUpdate(final JavaPlugin otherPlugin, VersionChecker versionChecker, String author, String githubProject, String jarname) {
        try {

            String tag = "";
            String s = "https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest";
            URL api = new URL(s);
            URLConnection con = api.openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);

            /*
                will retrieve version from Github updater first if possible
                if that fails, will retrieve version from spigotmc
             */

            int latestVersion;
            try {
                if (versionChecker.hasNewUpdate())
                    tag = versionChecker.getLatestVersion();
                else return false;
            } catch (Exception e1) {
                Main.getInstance().error("An error occurred while updating " + otherPlugin.getName());

                if (Main.debug)
                    e1.printStackTrace();

                return false;
            }

            latestVersion = Integer.parseInt(tag.replaceAll("\\.", ""));

            final int myVersion = Integer.parseInt(otherPlugin.getDescription().getVersion().replaceAll("\\.", ""));

            s = "https://github.com/" + author + "/" + githubProject + "/releases/download/"
                    + tag + "/" + jarname;
            final URL download = new URL(s);

            if (latestVersion > myVersion) {
                Main.getInstance().important("Found a new version of " + ChatColor.GOLD
                                + otherPlugin.getName() + "( " + tag + ")");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream in = download.openStream();

                            File pluginFile;

                            //pluginFile = new File(URLDecoder.decode(
                            //        this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                            //        StandardCharsets.UTF_8));
//otherPlugin.getPluginLoader().getPluginDescription()



                            pluginFile = new File(URLDecoder.decode(
                                    otherPlugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                                    StandardCharsets.UTF_8));

                            //Main.getInstance().info("path: " + pluginFile.getAbsolutePath());

                            //if (true)
                            //    return;

                            // Copy the current plugin to 'plugin-backup.jar'
                            File tempInCaseSomethingGoesWrong = new File(otherPlugin.getName() + "-backup.jar");
                            copy(new FileInputStream(pluginFile), new FileOutputStream(tempInCaseSomethingGoesWrong));


                            // Delete the old plugin,
                            pluginFile.setWritable(true, false);
                            pluginFile.delete();

                            // Write the new plugin to the old plugin
                            copy(in, new FileOutputStream(pluginFile));

                            if (pluginFile.length() < 1000) {
                                // Plugin is too small. Keep old version in case new one is
                                // incomplete/nonexistant
                                copy(new FileInputStream(tempInCaseSomethingGoesWrong),
                                        new FileOutputStream(pluginFile));
                            } else {
                                // Plugin is valid, and we can delete the temp
                                tempInCaseSomethingGoesWrong.delete();
                            }

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getPluginManager().disablePlugin(otherPlugin);



                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            //Bukkit.getPluginManager().enablePlugin(otherPlugin);
                                            try {
                                                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginFile));
                                            } catch (InvalidPluginException | InvalidDescriptionException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.runTaskLater(Main.getInstance(), 3 * 20);



                                }
                            }.run();



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskLaterAsynchronously(otherPlugin, 3 * 30);

                Main.getInstance().important("Updating " + otherPlugin.getName() + " in 3 seconds");

                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

