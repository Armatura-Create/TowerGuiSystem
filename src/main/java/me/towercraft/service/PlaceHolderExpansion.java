package me.towercraft.service;

import me.towercraft.TGS;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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

@Service
public class PlaceHolderExpansion extends PlaceholderExpansion {

    @Autowire
    private TGSLogger tgsLogger;

    @Autowire
    private TGS plugin;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @PostConstruct
    public void init() {
        if (plugin.getConfig().getBoolean("Enable.PlaceHolderApi"))
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                register();
                tgsLogger.log("PlaceHolderExpansion - registered");
            } else
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
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
        return "TowerGuiSystem";
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

        tgsLogger.log("PlaceHolderExpansion onRequest - " + params);

        if (params == null)
            return "";

        if (params.equals("servername")) {
            String servername = plugin.getServer().getName().split("-")[0];
            tgsLogger.log("Server name - " + servername);
            return servername;
        }

        if (params.equals("servernamewithnumber")) {
            return plugin.getServer().getName();
        }

        if (params.equals("onlineamount")) {
            Integer onlineServers = serversUpdateHandler.getServers()
                    .stream()
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
