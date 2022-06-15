package me.towercraft.service;

import me.towercraft.TGS;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.annotations.Service;
import me.towercraft.service.connect.ConnectionService;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.ui.gui.Gui;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuiService {

    @Autowire
    private TGS plugin;

    @Autowire
    private ConnectionService connectionService;

    @Autowire
    private ServersUpdateHandler serversUpdateHandler;

    @Autowire
    private FileMessages fileMessages;

    @Autowire
    private TGSLogger tgsLogger;

    private Map<String, Gui> guis;
    private Boolean enableGuis;

    @PostConstruct
    public void init() {
        guis = new ConcurrentHashMap<>();
        enableGuis = plugin.getConfig().getBoolean("Enable.Guis", false);
        load();
    }

    public void load() {
        if (!this.enableGuis) {
            return;
        }
        this.guis.clear();
        plugin.getServer().getScheduler().cancelTasks(plugin);

        final File files = new File(plugin.getDataFolder() + File.separator + "Menu");
        if (!files.exists()) {
            files.mkdir();
        }
        for (final File fileEntry : files.listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".yml")) {
                try {
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(fileEntry);
                    String command = configuration.getString("command", null);

                    tgsLogger.log("Loading Gui '" + fileEntry.getName().replace(".yml", "") + "'");

                    if (command == null) {
                        command = fileEntry.getName().replace(".yml", "").toLowerCase();
                    }

                    tgsLogger.log("Command - " + command);

                    if (command.split(":").length > 1 && command.split(":")[1].contains("dynamic")) {
                        File template = new File(plugin.getDataFolder() + File.separator + "Templates" + File.separator + configuration.getString("templates", null) + ".yml");
                        if (!template.exists()) {
                            template.createNewFile();
                            tgsLogger.log("File - " + configuration.getString("templates", null) + ".yml not found");
                        } else {
                            FileConfiguration templateConfiguration = YamlConfiguration.loadConfiguration(template);

                            this.guis.put(command.split(":")[0],
                                    new Gui(command.split(":")[0],
                                            templateConfiguration,
                                            configuration,
                                            plugin,
                                            connectionService,
                                            serversUpdateHandler,
                                            fileMessages,
                                            tgsLogger)
                            );
                        }
                    } else
                        this.guis.put(command,
                                new Gui(command,
                                        configuration,
                                        null,
                                        plugin,
                                        connectionService,
                                        serversUpdateHandler,
                                        fileMessages,
                                        tgsLogger)
                        );
                    Bukkit.getLogger().info("Gui '" + fileEntry.getName().replace(".yml", "") + "' successfully uploaded");
                } catch (Exception ex) {
                    Bukkit.getLogger().info("Error loading GUI - " + fileEntry.getName().replace(".yml", ""));
                    ex.printStackTrace();
                }
            }
        }
    }

    public Map<String, Gui> getGuis() {
        return guis;
    }
}
