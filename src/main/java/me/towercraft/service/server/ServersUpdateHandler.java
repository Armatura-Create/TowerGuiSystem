package me.towercraft.service.server;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import me.towercraft.TGS;
import me.towercraft.utils.TGSLogger;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.annotations.PreDestroy;
import unsave.plugin.context.annotations.Service;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServersUpdateHandler {

    @Autowire
    private TGS plugin;

    @Autowire
    private TGSLogger tgsLogger;

    private final List<ServerModel> servers = new ArrayList<>();
    private BukkitRunnable bukkitRunnable;

    @PostConstruct
    public void init() {
        tgsLogger.log("Start get servers");
        bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (servers) {
                    servers.clear();
                    tgsLogger.log("" + CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices().size());
                    for (ServiceInfoSnapshot cloudService : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices()) {
                        ServerModel.ServerModelBuilder modelBuilder = new ServerModel.ServerModelBuilder();
                        modelBuilder.group(cloudService.getName().split("-")[0]);
                        modelBuilder.dynamic(!cloudService.getConfiguration().isStaticService()); //TODO Понять как правильно сделать
                        modelBuilder.name(cloudService.getName());
                        modelBuilder.maxPlayers(cloudService.getProperty(BridgeServiceProperty.MAX_PLAYERS).orElse(0));
                        modelBuilder.nowPlayer(cloudService.getProperty(BridgeServiceProperty.ONLINE_COUNT).orElse(0));
                        modelBuilder.mapName(cloudService.getProperty(BridgeServiceProperty.MOTD).orElse("NameMap"));

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

                        tgsLogger.log(modelBuilder.build() + "");

                        servers.add(modelBuilder.build());
                    }
                }
            }
        };
        bukkitRunnable.runTaskTimer(plugin, 40L, plugin.getConfig().getLong("General.updateInterval", 2000L) / 50L);
    }

    @PreDestroy
    public void destroy() {
        bukkitRunnable.cancel();
    }

    public List<ServerModel> getServers() {
        synchronized (servers) {
            return new ArrayList<>(servers);
        }
    }
}
