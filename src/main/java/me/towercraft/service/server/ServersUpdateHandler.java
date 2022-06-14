package me.towercraft.service.server;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import me.towercraft.TGS;
import me.towercraft.plugin.ioc.annotations.Autowire;
import me.towercraft.plugin.ioc.annotations.PostConstruct;
import me.towercraft.plugin.ioc.annotations.PreDestroy;
import me.towercraft.plugin.ioc.annotations.Service;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServersUpdateHandler {

    @Autowire
    private TGS plugin;

    private final List<ServerModel> servers = new ArrayList<>();
    private BukkitRunnable bukkitRunnable;

    @PostConstruct
    public void init() {
        bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (servers) {
                    servers.clear();
                    for (ServiceInfoSnapshot cloudService : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices()) {
                        ServerModel.ServerModelBuilder modelBuilder = new ServerModel.ServerModelBuilder();
                        modelBuilder.group(cloudService.getName().split("-")[0]);
                        modelBuilder.dynamic(cloudService.getName().split("-").length > 1);
                        modelBuilder.name(cloudService.getName());
                        modelBuilder.maxPlayers(cloudService.getProperty(BridgeServiceProperty.MAX_PLAYERS).orElse(0));
                        modelBuilder.nowPlayer(cloudService.getProperty(BridgeServiceProperty.ONLINE_COUNT).orElse(0));
                        modelBuilder.mapName(cloudService.getProperty(BridgeServiceProperty.MOTD).orElse("MOTD"));

                        TypeStatusServer status = TypeStatusServer.OFFLINE;

                        if (cloudService.getProperty(BridgeServiceProperty.IS_ONLINE).orElse(false)) {
                            status = TypeStatusServer.ONLINE;

                            if (cloudService.getProperty(BridgeServiceProperty.IS_IN_GAME).orElse(false)) {
                                status = TypeStatusServer.IN_GAME;
                            }
                        } else if (cloudService.getProperty(BridgeServiceProperty.IS_STARTING).orElse(false)) {
                            status = TypeStatusServer.STARTING;
                        }

                        modelBuilder.status(status);
                    }
                }
            }
        };
        bukkitRunnable.runTaskTimer(plugin, 20L, plugin.getConfig().getLong("General.updateInterval") / 50L);
    }

    @PreDestroy
    public void stop() {
        synchronized (servers) {
            bukkitRunnable.cancel();
        }
    }

    public List<ServerModel> getServers() {
        synchronized (servers) {
            return servers;
        }
    }
}
