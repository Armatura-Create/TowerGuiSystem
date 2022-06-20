package me.towercraft.service;

import me.towercraft.TGS;
import me.towercraft.utils.TGSLogger;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.annotations.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Service
public class SpigotUpdater {
    private final int projectId = 76667;
    private URL checkURL;
    private String newVersion = "";
    @Autowire
    private TGS plugin;

    @Autowire
    private TGSLogger tgsLogger;

    @PostConstruct
    public void init() {
        this.newVersion = plugin.getDescription().getVersion();
        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.checkForUpdates();
    }

    public int getProjectID() {
        return projectId;
    }

    public String getLatestVersion() {
        return newVersion;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + projectId;
    }

    public void checkForUpdates() {
        try {
            URLConnection con = checkURL.openConnection();
            this.newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (!plugin.getDescription().getVersion().equals(newVersion))
                tgsLogger.log("An update was found! New version: " + getLatestVersion() + " download: " + getResourceURL());
        } catch (Exception e) {
            tgsLogger.log("Could not check for updates! Stacktrace:");
            e.printStackTrace();
        }
    }
}
