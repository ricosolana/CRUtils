package com.crazicrafter1.crutils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public enum GitUtils {
    ;

    public static String getTag(String author, String project) throws IOException {
        URL api = new URL("https://api.github.com/repos/" + author + "/" + project + "/releases/latest");
        URLConnection con = api.openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        JsonObject json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
        return json.get("tag_name").getAsString();
    }

    public static String getTagAsync(String author, String project, Consumer<String> callback) throws IOException {

        new BukkitRunnable() {
            @Override
            public void run() {



                try {
                    String tag = getTag(author, project);
                    accept(callback, tag);
                } catch (Exception e) {
                    accept(callback, null);
                }



            }
        }.runTaskAsynchronously(Main.getInstance());

        URL api = new URL("https://api.github.com/repos/" + author + "/" + project + "/releases/latest");
        URLConnection con = api.openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        JsonObject json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
        return json.get("tag_name").getAsString();
    }

    private static void installTag(String author, String project, String tag, String gitFilename, String filename, boolean replace) throws IOException {
        final URL download = new URL("https://github.com/" + author + "/" + project + "/releases/download/"
                + tag + "/" + gitFilename);

        InputStream gitStream = download.openStream();

        File root = new File(URLDecoder.decode(
                GitUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                "UTF-8")).getParentFile();

        File installFile = new File(root, filename);
        boolean exists = installFile.exists();
        if (replace || !exists) {

            // If the file already exists,
            // take some measures to make sure
            // it doesn't become corrupt
            //if (installFile.exists()) {
            if (exists) {

                // create backup
                File backup = new File(root, installFile.getName() + "-backup.jar");
                Util.copy(new FileInputStream(installFile), new FileOutputStream(backup));

                installFile.setWritable(true, false);
                installFile.delete();
                Util.copy(gitStream, new FileOutputStream(installFile));

                if (installFile.length() < 1000) {
                    // FALLBACK
                    Util.copy(new FileInputStream(backup), new FileOutputStream(installFile));
                    throw new InterruptedIOException("File was corrupted");
                } else {
                    // All is good
                    backup.delete();
                }
            } else {
                installFile.createNewFile();

                Util.copy(gitStream, new FileOutputStream(installFile));
            }
        }
    }



    @Deprecated
    public static void installLatestAsync(String author, String project, String filename, boolean replace, @Nonnull Consumer<String> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                StringBuilder outTag = new StringBuilder();

                try {
                    installLatest(author, project, filename, replace, outTag);
                } catch (IOException ignored) {}

                accept(callback, outTag.toString());
            }
        }.runTaskAsynchronously(Main.getInstance());
    }



    public static void installLatest(String author, String project, String filename, boolean replace) throws IOException {
        installLatest(author, project, filename, replace, null);
    }

    public static void installLatest(String author, String project, String filename, boolean replace, @Nullable StringBuilder outTag) throws IOException {
        String tagName = getTag(author, project);
        if (outTag != null) outTag.append(tagName);
        installTag(author, project, tagName, filename, filename, replace);
    }



    public static boolean updatePlugin(final Plugin plugin, String author, String project, String filename) throws IOException {
        return updatePlugin(plugin, author, project, filename, null);
    }

    public static boolean updatePlugin(final Plugin plugin, String author, String project, String filename, @Nullable StringBuilder outTag) throws IOException {
        String tag = getTag(author, project);
        if (outTag != null) outTag.append(tag);
        if (Util.outdatedSemver(plugin.getDescription().getVersion(), tag)) {
            installTag(author, project, tag, filename, filename, true);
            return true;
        }
        return false;
    }



    @Deprecated
    public static void updatePluginAsync(final Plugin plugin, String author, String project, String filename, @Nonnull BiConsumer<Boolean, String> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {


                StringBuilder outTag = new StringBuilder();
                try {
                    boolean result = updatePlugin(plugin, author, project, filename, outTag);
                    accept(callback, result, outTag.toString());
                } catch (IOException e) {
                    accept(callback, false, outTag.toString());
                }


            }
        }.runTaskAsynchronously(Main.getInstance());
    }



    public static boolean checkForUpdate(final Plugin plugin, String author, String project) throws IOException {
        return checkForUpdate(plugin, author, project, null);
    }

    public static boolean checkForUpdate(final Plugin plugin, String author, String project, @Nullable StringBuilder outTag) throws IOException {
        String tag = getTag(author, project);
        if (outTag != null) outTag.append(tag);
        return Util.outdatedSemver(plugin.getDescription().getVersion(), tag);
    }

    public static void checkForUpdateAsync(final Plugin plugin, String author, String project, @Nonnull BiConsumer<Boolean, String> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {

                try {
                    StringBuilder outTag = new StringBuilder();
                    boolean result = checkForUpdate(plugin, author, project, outTag);

                    accept(callback, result, outTag.toString());
                } catch (Exception e) {
                    accept(callback, false, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }



    private static void accept(@Nonnull BiConsumer<Boolean, String> callback, boolean b, @Nullable String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                callback.accept(b, (s == null || s.isEmpty())
                        ? null
                        : s);
            }
        }.runTask(Main.getInstance());
    }

    private static void accept(@Nonnull Consumer<String> callback, @Nullable String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                callback.accept((s == null || s.isEmpty())
                        ? null
                        : s);
            }
        }.runTask(Main.getInstance());
    }

}
