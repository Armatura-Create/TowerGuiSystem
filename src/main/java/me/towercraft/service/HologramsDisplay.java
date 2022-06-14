package me.towercraft.service;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import me.towercraft.TGS;
import me.towercraft.plugin.ioc.annotations.Autowire;
import me.towercraft.plugin.ioc.annotations.PostConstruct;
import me.towercraft.plugin.ioc.annotations.PreDestroy;
import me.towercraft.plugin.ioc.annotations.Service;
import me.towercraft.service.server.ServerModel;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.List;

import static com.gmail.filoghost.holographicdisplays.api.HologramsAPI.*;

@Service
public class HologramsDisplay {

    @Autowire
    private TGSLogger TGSLogger;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @Autowire
    private TGS plugin;

    @PostConstruct
    public void init() {
        if (plugin.getConfig().getBoolean("Enable.HologramsDisplay"))
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

    public boolean registerPlaceholderPlugin(String textPlaceholder, double refreshRate, PlaceholderReplacer replacer) {
        return registerPlaceholder(plugin, textPlaceholder, refreshRate, replacer);
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
                    for (String placeholder : placeHolders) {

                        Integer onlineServers = serversUpdateHandler.getServers()
                                .stream()
                                .filter(s -> s.getName().split("-")[0].equalsIgnoreCase(placeholder))
                                .map(ServerModel::getNowPlayer)
                                .reduce(0, Integer::sum);

                        boolean isOnline = serversUpdateHandler.getServers()
                                .stream()
                                .filter(s -> s.getName().split("-")[0].equalsIgnoreCase(placeholder)).findFirst().orElse(null) != null;

                        registerPlaceholderPlugin("{" + placeholder + "}", 1, () -> isOnline ? "§cOffline" : "" + onlineServers);
                        TGSLogger.log("Register Placeholder - " + placeholder);
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
