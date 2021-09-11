package com.crazicrafter1.crutils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class GithubUpdater {

    public static boolean autoUpdate(final JavaPlugin plugin, VersionChecker versionChecker, String author, String githubProject, String jarname) {
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
                Main.getInstance().error("An error occurred while updating " + plugin.getName());

                if (Main.debug)
                    e1.printStackTrace();

                return false;
            }

            latestVersion = Integer.parseInt(tag.replaceAll("\\.", ""));

            final int myVersion = Integer.parseInt(plugin.getDescription().getVersion().replaceAll("\\.", ""));

            s = "https://github.com/" + author + "/" + githubProject + "/releases/download/"
                    + tag + "/" + jarname;
            final URL download = new URL(s);

            if (latestVersion > myVersion) {
                Main.getInstance().important(ChatColor.GREEN + "Found a new version of " + ChatColor.GOLD
                                + plugin.getDescription().getName() + ": " + ChatColor.WHITE + tag
                                + ChatColor.LIGHT_PURPLE + " downloading now!");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream in = download.openStream();

                            File pluginFile;

                            pluginFile = new File(URLDecoder.decode(
                                    this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                                    StandardCharsets.UTF_8));

                            Main.getInstance().info("path: " + pluginFile.getAbsolutePath());

                            if (true)
                                return;

                            // Copy the current plugin to 'plugin-backup.jar'
                            File tempInCaseSomethingGoesWrong = new File(plugin.getName() + "-backup.jar");
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

                            //Bukkit.getPluginManager().disablePlugin(plugin);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(plugin);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static long copy(InputStream in, OutputStream out) throws IOException {
        long bytes = 0;
        byte[] buf = new byte[0x1000];
        while (true) {
            int r = in.read(buf);
            if (r == -1)
                break;
            out.write(buf, 0, r);
            bytes += r;
        }
        out.flush();
        out.close();
        in.close();
        return bytes;
    }

}

