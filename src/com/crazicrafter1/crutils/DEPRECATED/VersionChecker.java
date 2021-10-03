package com.crazicrafter1.crutils.DEPRECATED;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VersionChecker {
    private int project = 0;
    private URL checkURL;
    private String latestVersion = "";
    private String myVersion = "";

    //private JavaPlugin plugin;

    public VersionChecker(JavaPlugin plugin, int projectID) {
        //this.plugin = plugin;
        this.project = projectID;
        this.myVersion = plugin.getDescription().getVersion();

        try { //o28
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID + "/");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + this.project;
    }

    public boolean hasNewUpdate() throws Exception {
        URLConnection con = this.checkURL.openConnection();
        this.latestVersion = (new BufferedReader(new InputStreamReader(con.getInputStream()))).readLine();
        return Integer.parseInt(latestVersion.replaceAll("\\.", "")) >
                Integer.parseInt(myVersion.replaceAll("\\.", ""));
    }
}
