package com.towercraft;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.google.common.collect.Ordering;
import com.towercraft.gui.Gui;
import com.towercraft.items.ItemListener;
import com.towercraft.items.ItemManager;
import com.towercraft.utils.ServerModel;
import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.server.info.ServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class TowerGuiSystem extends JavaPlugin implements CommandExecutor {

    public static TowerGuiSystem instance;
    private HashMap<String, Gui> guis;
    public ItemManager itemManager;
    public static TowerGuiSystem plugin;
    private String source;
    boolean gui;
    boolean items;
    public boolean clearOnJoin;
    public static String nameServer;
    public static List<ServerModel> servers;
    public static List<ServerModel> lobbys;
    public static Map<String, String> serversOnline;
    public static Map<String, String> lobbysOnline;
    public static String prefix;
    Thread updater;
    Runnable mainRunnable;

    public static String getPrefix() {
        return TowerGuiSystem.prefix;
    }

    public void startUpdate() {
        (this.updater = new Thread(() -> {
            long tick = 0L;
            outer:
            while (true) {
                while (true) {
                    try {
                        while (true) {
                            Thread.sleep(50L);
                            if (tick == Long.MAX_VALUE) {
                                tick = 0L;
                            }
                            if (tick == 0L || tick % 100L == 0L) {
                                TowerGuiSystem.this.mainRunnable.run();
                            }
                            ++tick;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue outer;
                    }
                }
            }
        })).start();
    }

    void staticRunnable() {
        this.mainRunnable = () -> {
            if (TimoCloudAPI.getBukkitAPI() != null)
                if (TimoCloudAPI.getBukkitAPI().getThisServer() != null)
                    nameServer = TimoCloudAPI.getBukkitAPI().getThisServer().getName();
            servers.clear();
            lobbys.clear();
            for (String group : instance.getConfig().getConfigurationSection("general").getStringList("name_group")) {
                if (getOnlineServers(group.split(":")[0]).size() > 0) {
                    if (group.split(":").length > 1) {
                        lobbys.addAll(getOnlineServers(group.split(":")[0]));
                        for (final ServerModel s : getOnlineServers(group.split(":")[0]))
                            TowerGuiSystem.lobbysOnline.put(s.getName(), s.getNowPlayer() + "/" + s.getMaxPlayers());
                    } else {
                        servers.addAll(getOnlineServers(group));

                        for (int i = 0; i < getOnlineServers(group).size(); i++) {
                            ServerModel temp = getOnlineServers(group).get(i);
                            if (temp.getName().split("-").length > 1 && i == 0)
                                TowerGuiSystem.serversOnline.put(getOnlineServers(group).get(i).getName().split("-")[0], 0 + "/" + 0);
                            else if (temp.getName().split("-").length == 1)
                                TowerGuiSystem.serversOnline.put(temp.getName().split("-")[0], 0 + "/" + 0);
                            int now = (TowerGuiSystem.serversOnline.get(temp.getName().split("-")[0]) != null ? Integer.parseInt(TowerGuiSystem.serversOnline.get(temp.getName().split("-")[0]).split("/")[0]) : 0) + temp.getNowPlayer();
                            int max = (TowerGuiSystem.serversOnline.get(temp.getName().split("-")[0]) != null ? Integer.parseInt(TowerGuiSystem.serversOnline.get(temp.getName().split("-")[0]).split("/")[1]) : 0) + temp.getMaxPlayers();
                            TowerGuiSystem.serversOnline.put(temp.getName().split("-")[0], now + "/" + max);

                        }
                    }
                } else if (group.split(":").length == 1)
                    TowerGuiSystem.serversOnline.put(group, 0 + "/" + 0);
            }
        };
    }

    List<ServerModel> getOnlineServers(String nameGroup) {
        List<ServerModel> result = new ArrayList<>();

        switch (source) {
            case "timocloud":
                if (TimoCloudAPI.getUniversalAPI() != null && TimoCloudAPI.getUniversalAPI().getServerGroup(nameGroup) != null)
                    for (ServerObject temp :
                            TimoCloudAPI.getUniversalAPI().getServerGroup(nameGroup).getServers()) {
                        if (!temp.getState().equalsIgnoreCase("ingame")) {
                            ServerModel model = new ServerModel();
                            model.setName(temp.getName());
                            model.setGroup(nameGroup);
                            model.setMaxPlayers(temp.getMaxPlayerCount());
                            model.setNowPlayer(temp.getOnlinePlayerCount());
                            model.setStatus(temp.getState().equalsIgnoreCase("online") ? "Waiting" : temp.getState());
                            model.setMap(temp.getMap());
                            result.add(model);
                        }
                    }
                break;
            case "cloudnet":

                if (CloudAPI.getInstance() != null && CloudAPI.getInstance().getServers(nameGroup) != null)
                    for (ServerInfo temp :
                            CloudAPI.getInstance().getServers(nameGroup)) {
                        ServerModel model = new ServerModel();
                        model.setName(temp.getServerConfig().getProperties().getName());
                        model.setGroup(nameGroup);
                        model.setMaxPlayers(temp.getMaxPlayers());
                        model.setNowPlayer(temp.getOnlineCount());
                        model.setStatus(temp.isIngame() ? "" : "");
                        model.setMap(temp.isIngame() ? "InGame" : "Waiting");
                        result.add(model);
                    }
                result = new ArrayList<>();
                break;
            default:
                log("Нет такого source - " + source);
                this.setEnabled(false);
                break;
        }

//        result.sort(Ordering.usingToString());
        return result;
    }

    void loadGui() {
        if (!this.gui) {
            return;
        }
        this.guis.clear();
        this.getServer().getScheduler().cancelTasks(this);
        final File files = new File(TowerGuiSystem.instance.getDataFolder() + File.separator + "Menu");
        if (!files.exists()) {
            files.mkdir();
        }
        for (final File fileEntry : files.listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".yml")) {
                try {
                    final FileConfiguration configuration = YamlConfiguration.loadConfiguration(fileEntry);
                    String command = configuration.getString("command", null);
                    if (command == null) {
                        command = fileEntry.getName().replace(".yml", "");
                    }
                    Bukkit.getLogger().info("Загружаю Gui '" + fileEntry.getName().replace(".yml", "") + "'");
                    if (command.split(":").length > 1 && command.split(":")[1].contains("dynamic")) {
                        File templates = new File(TowerGuiSystem.instance.getDataFolder() + File.separator + "Templates" + File.separator + configuration.getString("templates", null) + ".yml");
                        if (!templates.exists()) {
                            templates.mkdir();
                            log("Файл - " + configuration.getString("template", null) + ".yml не найден");
                        }
                        this.guis.put(command.split(":")[0], new Gui(command.split(":")[0], configuration, YamlConfiguration.loadConfiguration(templates)));
                    } else
                        this.guis.put(command, new Gui(command, configuration));
                    Bukkit.getLogger().info("Gui '" + fileEntry.getName().replace(".yml", "") + "' успешно загружено");
                } catch (Exception ex) {
                    Bukkit.getLogger().info("ошибка при загрузки GUI - " + fileEntry.getName().replace(".yml", ""));
                    ex.printStackTrace();
                }
            }
        }
    }

    void loadItems() {
        if (!this.items) {
            return;
        }
        this.clearOnJoin = this.getConfig().getBoolean("ClearInventoryOnJoin", false);
        this.itemManager = new ItemManager();
        new ItemListener();
    }

    public static void connect(final Player p, String where) {

        String type = where.split("_")[1];

        List<ServerModel> servers = new ArrayList<>();

        TowerGuiSystem.servers.sort(Comparator.comparing(ServerModel::getNowPlayer));

        for (final ServerModel s : TowerGuiSystem.servers)
            if (s.getName().contains(where.split("_")[0])
                    && (s.getInStatus().equalsIgnoreCase("online") || s.getInStatus().equalsIgnoreCase("waiting")))
                servers.add(s);

        System.out.println(where);

        if (servers.size() < 1)
            p.sendMessage(getPrefix() + "Не найден сервер");

        switch (type) {
            case "random":
                Collections.shuffle(servers);
                break;

            case "max":
                Collections.reverse(servers);
                break;
        }

        if (servers.size() > 0) {

            if (p.getServer().getName().equalsIgnoreCase(servers.get(0).getName())) {
                p.sendMessage(Arrays.toString(new ComponentBuilder("Вы уже находитесь на - " + where.split("_")[0]).color(ChatColor.RED).create()));
                return;
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            try {
                out.writeUTF("Connect");
                out.writeUTF(servers.get(0).getName());
                System.out.println(servers.get(0).getName());
            } catch (IOException e) {
                log(e.getMessage());
            }

            p.sendPluginMessage(TowerGuiSystem.instance, "BungeeCord", b.toByteArray());
        }
    }

    public static void registerListener(final Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, TowerGuiSystem.instance);
    }

    public static void log(final String message) {
        Logger.getLogger("Minecraft").info("[TowerGuiSystem] " + message);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        switch (commandLabel.split("_")[0]) {
            case "gui":
                if (args.length == 0) {
                    sender.sendMessage(getPrefix() + "Открыть Gui - §c/gui open [название]");
                    sender.sendMessage(getPrefix() + "Список Gui - §c/gui list");
                    return true;
                }
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("list")) {
                        sender.sendMessage(getPrefix() + "Список Gui:");
                        for (final String name : this.guis.keySet()) {
                            sender.sendMessage("§f- §c" + name);
                        }
                        return true;
                    }
                }

                if (!(sender instanceof Player)) {
                    return false;
                }

                if (args.length == 2) {
                    final Player player = (Player) sender;
                    if (args[0].equalsIgnoreCase("open")) {
                        final Gui gui = this.guis.get(args[1]);
                        if (gui == null) {
                            sender.sendMessage(getPrefix() + "§cGui не найденв!");
                            return true;
                        }
                        gui.open(player);
                        return true;
                    }
                }
                sender.sendMessage(getPrefix() + "Открыть Gui - §c/gui open [название]");
                sender.sendMessage(getPrefix() + "Список Gui - §c/gui list");
                return true;

            case "connect":
                if (sender instanceof Player)
                    if (args.length > 0)
                        connect((Player) sender, args[0] + "_normal");
                    else
                        sender.sendMessage(getPrefix() + "Не задан сервер");
                return true;

            default:
                sender.sendMessage(getPrefix() + "Команда не найденна");
                return true;
        }
    }

    @Override
    public void onEnable() {

        //TODO Добавить проверку на все нужные плагины

        this.saveDefaultConfig();
        TowerGuiSystem.plugin = this;
        TowerGuiSystem.instance = this;
        this.guis = new HashMap<>();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.gui = this.getConfig().getBoolean("Enable.Gui");
        this.items = this.getConfig().getBoolean("Enable.Items");
        this.source = this.getConfig().getString("general.source");
        this.loadItems();
        this.staticRunnable();
        this.startUpdate();
        this.getCommand("gui").setExecutor(this);
        this.getCommand("connect").setExecutor(this);
        loadGui();
    }

    static {
        TowerGuiSystem.servers = new ArrayList<>();
        TowerGuiSystem.nameServer = "";
        TowerGuiSystem.lobbys = new ArrayList<>();
        TowerGuiSystem.serversOnline = new HashMap<>();
        TowerGuiSystem.lobbysOnline = new HashMap<>();
        TowerGuiSystem.prefix = "§f[§eTowerGuiMenu§f] ";
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
    }
}
