package me.towercraft.ui.gui;

import me.towercraft.TGS;
import me.towercraft.service.FileMessages;
import me.towercraft.service.NameServerService;
import me.towercraft.service.connect.ConnectionService;
import me.towercraft.service.server.ServerModel;
import me.towercraft.service.server.ServersUpdateHandler;
import me.towercraft.service.server.TypeStatusServer;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Gui {
    private final String name;
    private String displayName;
    private final Map<Integer, GuiItem> items;
    private Inventory inventory;
    public String serverName;
    private final TGS plugin;
    private final FileConfiguration config;
    private final ServersUpdateHandler serversUpdateHandler;
    private final NameServerService nameServerService;
    private final FileMessages fileMessages;
    private final TGSLogger tgsLogger;

    public Gui(String name,
               FileConfiguration config,
               FileConfiguration template,
               TGS plugin,
               ConnectionService connectionService,
               ServersUpdateHandler serversUpdateHandler,
               NameServerService nameServerService,
               FileMessages fileMessages,
               TGSLogger tgsLogger) {
        this.config = config;
        this.name = name.replace("&", "§");
        this.items = new ConcurrentHashMap<>();
        this.plugin = plugin;
        this.nameServerService = nameServerService;
        this.serversUpdateHandler = serversUpdateHandler;
        this.fileMessages = fileMessages;
        this.tgsLogger = tgsLogger;

        if (config == null) {
            this.tgsLogger.log("Error loading GUI '" + this.name + "'. template is null");
            return;
        }

        if (template != null)
            this.displayName = template.getString("name", "&0GUI").replace("&", "§");
        else
            this.displayName = this.config.getString("name", this.name).replace("&", "§");

        this.inventory = Bukkit.createInventory(null, this.config.getInt("rows", 6) * 9, this.displayName);

        loadGui();

        if (template != null)
            startDynamic(template);

        new GuiListener(this, this.plugin, connectionService);
    }

    private void startDynamic(FileConfiguration template) {
        new BukkitRunnable() {
            @Override
            public void run() {

                int countName = 0;

                String configNameserver = template.getString("item.nameserver");

                if (configNameserver == null) {
                    tgsLogger.log("Not found String [item.nameserver] in template - " + template.getName());
                    return;
                }

                List<ServerModel> dynamicServers = serversUpdateHandler.getServers()
                        .stream()
                        .filter(serverModel -> serverModel.getName()
                                .contains(configNameserver.substring(2)))
                        .collect(Collectors.toList());

                int plus = 0;
                for (int i = 10; i < (template.getInt("rows") - 1) * 9; i++) {

                    if (i % 9 == 0 || i == 17 || i == 26 || i == 35 || i == 44) {
                        plus++;
                        continue;
                    }

                    items.remove(i);
                    inventory.clear(i);
                    countName++;

                    if (i < dynamicServers.size() + 10 + plus) {

                        ServerModel dynamicServer = dynamicServers.get(countName - 1);

                        if (dynamicServer != null)
                            try {
                                //В зависимости от того сколько игроков подгружаем нужный id предмета
                                String id = template.getStringList("items").get(0);

                                if (dynamicServer.getNowPlayer() >=
                                        template.getIntegerList("item.count").get(1))
                                    id = template.getStringList("items").get(1);

                                if (dynamicServer.getNowPlayer() >=
                                        template.getIntegerList("item.count").get(2))
                                    id = template.getStringList("items").get(2);

                                if (dynamicServer.getStatus() == TypeStatusServer.STARTING || dynamicServer.getStatus() == TypeStatusServer.OFFLINE)
                                    id = template.getStringList("items").get(3);

                                if (dynamicServer.getStatus() == TypeStatusServer.IN_GAME)
                                    id = template.getStringList("items").get(4);

                                String name = template.getString("item.nameserver").replace("&", "§") + "-" + countName;
                                List<String> lore_config = template.getStringList("item.lore");
                                List<String> lore_result = new ArrayList<>();

                                for (String temp : lore_config) {
                                    if (nameServerService.getNameServer().equalsIgnoreCase(dynamicServer.getName())) {
                                        lore_result.add(temp.replace("%place%", "Вы находитесь здесь"));
                                        id = template.getStringList("items").get(5);
                                    } else
                                        lore_result.add(temp.replace("%place%", ""));
                                }

                                final String command = "server:" + dynamicServers.get(countName - 1).getName();
                                serverName = name;

                                GuiItem guiItem = new GuiItem(id, dynamicServer.getNowPlayer(), name, lore_result, i, command, new ArrayList<>(), dynamicServers.get(countName - 1), serverName);

                                ItemMeta meta = guiItem.getItemStack().getItemMeta();
                                List<String> description = new ArrayList<>();

                                for (String l : guiItem.getLore()) {
                                    String line = l.replace("%so%", "" + dynamicServer.getNowPlayer() + "/" + dynamicServer.getMaxPlayers())
                                            .replace("%map%", dynamicServer.getMapName())
                                            .replace("%status%", ChatColor.translateAlternateColorCodes('&', fileMessages.getMSG().getString("GUI.serverStatus." + dynamicServer.getStatus().getNameConfig())));
                                    description.add(line);
                                }

                                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                                meta.setLore(description);
                                guiItem.getItem().setItemMeta(meta);
                                items.put(i, guiItem);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }

                for (int s : items.keySet()) {
                    try {
                        inventory.setItem(s, items.get(s).getItemStack());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 1000L / 50);
    }

    void loadGui() {
        if (this.config == null) {
            tgsLogger.log("Error loading GUI '" + this.name + "'. config is null");
            return;
        }

        if (this.config.getConfigurationSection("Items") == null) {
            tgsLogger.log("Error loading GUI '" + this.name + "'. Items not found");
            return;
        }
        for (String itemName : this.config.getConfigurationSection("Items").getKeys(false)) {
            try {
                tgsLogger.log("Loading animation from item " + itemName);

                int slot = this.config.getInt("Items." + itemName + ".slot") - 1;
                String id = this.config.getString("Items." + itemName + ".id");
                String name = this.config.getString("Items." + itemName + ".name").replace("&", "§");
                List<String> description = this.config.getStringList("Items." + itemName + ".lore");
                String command = this.config.getString("Items." + itemName + ".command");
                int amount = this.config.getInt("Items." + itemName + ".amount", 1);
                serverName = this.config.getString("Items." + itemName + ".server");

                List<List<String>> animation = new ArrayList<>();
                for (String string : this.config.getConfigurationSection("Items." + itemName + ".animation").getKeys(false)) {
                    List<String> list = new ArrayList<>();
                    for (String str : this.config.getStringList("Items." + itemName + ".animation." + string)) {
                        list.add(str.replace("&", "§"));
                    }
                    animation.add(list);
                }
                tgsLogger.log("List of animation - " + animation.size());
                GuiItem guiItem = new GuiItem(
                        id,
                        amount,
                        name,
                        description,
                        slot,
                        command,
                        animation,
                        serversUpdateHandler.getServers()
                                .stream()
                                .filter(serverModel -> serverModel.getName().equals(serverName))
                                .findFirst()
                                .orElse(null),
                        serverName
                );

                this.items.put(slot, guiItem);
                this.update(guiItem);

                for (int s : this.items.keySet()) {
                    try {
                        this.inventory.setItem(s,
                                this.items.get(s).getItemStack());
                    } catch (Exception ex) {
                        tgsLogger.log("Item in slot '" + s + "' throw exception!");
                        ex.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                tgsLogger.log("Error from loading item '" + itemName + "' in GUI '" + this.name + "'");
            }
        }
    }

    void update(GuiItem item) {
        new BukkitRunnable() {
            public void run() {
                ItemMeta meta = item.getItemStack().getItemMeta();
                List<String> descriptions = new ArrayList<>();

                List<ServerModel> servers = new ArrayList<>();
                for (String server : item.getServer().split(":")) {
                    serversUpdateHandler.getServers()
                            .stream()
                            .filter(s -> s.getName().contains(server))
                            .filter(s -> s.getStatus() == TypeStatusServer.ONLINE)
                            .findFirst()
                            .ifPresent(servers::add);
                }

                int allCount = servers.stream().map(ServerModel::getMaxPlayers).reduce(0, Integer::sum);
                int onlineCount = servers.stream().map(ServerModel::getNowPlayer).reduce(0, Integer::sum);

                String online = servers.size() == 0 ? "§cOffline" : onlineCount + "/" + allCount;

                for (String l : item.getLore()) {
                    String line = l.replace("%so%", online);
                    descriptions.add(line);
                }

                if (!item.iterator.hasNext()) {
                    List<List<String>> temp = new ArrayList<>();

                    for (int i = 0; i < item.getAnimation().size(); i++) {
                        List<String> replacement = new ArrayList<>();
                        for (int j = 0; j < item.getAnimation().get(i).size(); j++) {
                            replacement.add(item.getAnimation().get(i).get(j)
                                    .replace("%so%", "" + online)
                                    .replace("%sa%", "" + serversUpdateHandler.getServers()
                                            .stream()
                                            .filter(s -> s.getName().split("-")[0].equalsIgnoreCase(item.getServer()))
                                            .count())
                            );
                        }
                        temp.add(replacement);
                    }

                    item.iterator = temp.iterator();
                }

                descriptions.addAll(item.iterator.next());

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

                meta.setLore(descriptions);
                item.getItem().setItemMeta(meta);
                Gui.this.inventory.setItem(item.getSlot(), item.getItemStack());
            }
        }.runTaskTimer(plugin, 40L, plugin.getConfig().getInt("AnimationTime", 20));
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    public GuiItem getItem(int slot) {
        return this.items.get(slot);
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }
}
