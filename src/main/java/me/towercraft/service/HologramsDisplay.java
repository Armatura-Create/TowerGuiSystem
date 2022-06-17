package me.towercraft.service;

import me.towercraft.TGS;
import me.towercraft.service.server.ServerModel;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.service.server.TypeStatusServer;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;

import java.util.ArrayList;
import java.util.List;

import static com.gmail.filoghost.holographicdisplays.api.HologramsAPI.registerPlaceholder;

public class HologramsDisplay {

    @Autowire
    private TGSLogger tgsLogger;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @Autowire
    private TGS plugin;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (true) {
                try {
                    Plugin holographicDisplays = Bukkit.getPluginManager().getPlugin("HolographicDisplays");

                    if (Bukkit.getPluginManager().isPluginEnabled(holographicDisplays)) {
                        for (String piece : plugin.getConfig().getStringList("Data.HologramsDisplay")) {
                            registerPlaceholder(plugin, "{" + piece + "}", 1,
                                    () -> {
                                        List<ServerModel> servers = new ArrayList<>();
                                        for (String p : piece.split(":")) {
                                            serversUpdateHandler.getServers()
                                                    .stream()
                                                    .filter(s -> s.getName().contains(p))
                                                    .filter(s -> s.getStatus() == TypeStatusServer.ONLINE)
                                                    .findFirst()
                                                    .ifPresent(servers::add);
                                        }

                                        boolean finalIsOffline = servers.size() == 0;
                                        int finalOnlineCount = servers
                                                .stream()
                                                .map(ServerModel::getNowPlayer)
                                                .reduce(0, Integer::sum);
                                        return finalIsOffline ? "§cOffline" : "" + finalOnlineCount;
                                    });
                            tgsLogger.log("Register Placeholder - " + piece);
                        }
                        return;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (Exception ignore) {
                }
            }
        }).start();
    }
}
