package me.towercraft.service;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.towercraft.TGS;
import me.towercraft.service.server.TypeStatusServer;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.annotations.PreDestroy;
import unsave.plugin.context.annotations.Service;
import me.towercraft.service.server.ServerModel;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.gmail.filoghost.holographicdisplays.api.HologramsAPI.*;

@Service
public class HologramsDisplay {

    @Autowire
    private TGSLogger tgsLogger;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @Autowire
    private TGS plugin;

    @PostConstruct
    public void init() {
        if (plugin.getConfig().getBoolean("Enable.HologramsDisplay", false))
            if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
                registerPlaceholderPluginAll(plugin.getConfig().getStringList("Data.HologramsDisplay"));
            } else
                throw new RuntimeException("Could not find HolographicDisplays!! Plugin can not work without it!");
    }

    public Hologram createHologramPlugin(Location location) {
        return createHologram(plugin, location);
    }

    public Collection<Hologram> getHologramsPlugin() {
        return getHolograms(plugin);
    }

    public Collection<String> getRegisteredPlaceholdersPlugin() {
        return getRegisteredPlaceholders(plugin);
    }

    public boolean isHologramEntityPlugin(Entity entity) {
        return isHologramEntity(entity);
    }

    public void registerPlaceholderPluginAll(List<String> placeHolders) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    for (String piece : placeHolders) {
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
                } catch (Exception ignore) {
                }
            }
        }).start();
    }

    @PreDestroy
    public void destroy() {
        unregisterPlaceholders(plugin);
    }
}
