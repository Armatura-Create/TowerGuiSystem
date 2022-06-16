package me.towercraft.service;

import me.towercraft.TGS;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.towercraft.service.server.TypeStatusServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.annotations.PreDestroy;
import unsave.plugin.context.annotations.Service;
import me.towercraft.service.server.ServerModel;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaceHolderExpansion extends PlaceholderExpansion {

    @Autowire
    private TGSLogger tgsLogger;

    @Autowire
    private TGS plugin;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @Autowire
    private NameServerService nameServerService;

    @PostConstruct
    public void init() {
        if (plugin.getConfig().getBoolean("Enable.PlaceHolderApi")) {
            Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderAPI != null) {
                PluginManager pm = Bukkit.getPluginManager();
                tgsLogger.log("PlaceholderAPI start detected");
                new Thread(() -> {
                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (pm.isPluginEnabled(placeholderAPI)) {
                            tgsLogger.log("PlaceholderAPI detected enable");
                            this.register();
                            tgsLogger.log("PlaceHolderExpansion - registered");
                            break;
                        }
                    }
                }).start();
            } else
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
        }
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "towerguisystem";
    }

    @Override
    public String getRequiredPlugin() {
        return plugin.getDescription().getName();
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer p, String params) {

        if (params == null)
            return "";

        if (params.equals("servername")) {
            return nameServerService.getNameServer().split("-")[0];
        }

        if (params.equals("servernamewithnumber")) {
            return nameServerService.getNameServer();
        }

        if (params.equals("onlineamount")) {
            Integer onlineServers = serversUpdateHandler.getServers()
                    .stream()
                    .filter(s -> s.getStatus() == TypeStatusServer.ONLINE)
                    .map(ServerModel::getNowPlayer)
                    .reduce(0, Integer::sum);
            return onlineServers + "";
        }

        if (params.contains("serveramount")) {
            return serversUpdateHandler.getServers()
                    .stream()
                    .filter(s -> s.getName().split("-")[0].equalsIgnoreCase(params.split("_")[1]))
                    .count() + "";
        }
        return super.onRequest(p, params);
    }
}
