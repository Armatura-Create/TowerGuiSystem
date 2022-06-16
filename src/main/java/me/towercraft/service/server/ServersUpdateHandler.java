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

import java.util.ArrayList;
import java.util.List;

@Service
public class ServersUpdateHandler {

    @Autowire
    private TGS plugin;

    @Autowire
    private TGSLogger tgsLogger;

    private final List<ServerModel> servers = new ArrayList<>();
    private Thread workThread;

    @PostConstruct
    public void init() {
        tgsLogger.log("Start get servers");
        workThread = new Thread(() -> {
            while (true) {
                synchronized (servers) {
                    servers.clear();
                    for (ServiceInfoSnapshot cloudService : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices()) {
                        ServerModel.ServerModelBuilder modelBuilder = new ServerModel.ServerModelBuilder();
                        modelBuilder.group(cloudService.getName().split("-")[0]);
                        modelBuilder.dynamic(!cloudService.getConfiguration().isStaticService());
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
                        servers.add(modelBuilder.build());
                    }
                }
                try {
                    Thread.sleep(plugin.getConfig().getLong("General.updateInterval"));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        workThread.start();
    }

    @PreDestroy
    public void destroy() {

        tgsLogger.log("Stop get servers");

        if (workThread != null && workThread.isAlive() && !workThread.isInterrupted())
            workThread.interrupt();
    }

    public List<ServerModel> getServers() {
        synchronized (servers) {
            return new ArrayList<>(servers);
        }
    }

    public int getCountAllOnlineByGroup(String group) {
        synchronized (servers) {
           return servers
                    .stream()
                    .filter(s -> s.getName().contains(group))
                    .map(ServerModel::getMaxPlayers)
                    .reduce(0, Integer::sum);
        }
    }

    public int getCountOnlineByGroup(String group) {
        synchronized (servers) {
            return servers
                    .stream()
                    .filter(s -> s.getName().contains(group))
                    .map(ServerModel::getNowPlayer)
                    .reduce(0, Integer::sum);
        }
    }
}
