package me.towercraft;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.towercraft.gui.Gui;
import me.towercraft.items.ItemListener;
import me.towercraft.items.ItemManager;
import me.towercraft.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class TGS extends JavaPlugin implements CommandExecutor, PluginMessageListener {

    public static TGS instance;
    public static Files files;
    private HashMap<String, Gui> guis;
    public ItemManager itemManager;
    public static TGS plugin;
    public long updateTime;
    boolean gui;
    boolean items;
    public boolean clearOnJoin;
    public boolean replaceItemOnJoin = false;
    public static boolean isPlaceholder;
    public static boolean isUpdate = false;

    HologramsDisplay hologramsDisplay;

    public static String nameServer;
    public static List<ServerModel> servers;
    public static List<ServerModel> lobbys;
    public static Map<String, String> serversOnline;
    public static Map<String, String> lobbysOnline;
    public static String prefix;
    Thread updater;
    Runnable mainRunnable;

    public static String getPrefix() {
        return TGS.prefix;
    }

    public void startUpdate() {
        (this.updater = new Thread(() -> {
            while (true) {
                try {
                    while (true) {
                        Thread.sleep(updateTime); //Update UI in two seconds
                        if (Bukkit.getOnlinePlayers().toArray().length > 0 && nameServer == null)
                            setCurrentServer();
                        if (nameServer != null && !isUpdate)
                            TGS.this.mainRunnable.run();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        })).start();
    }

    void staticRunnable() {
        this.mainRunnable = () -> {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(b);

            try {
                out.writeUTF(nameServer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            getServer().sendPluginMessage(plugin, "tgs:channel", b.toByteArray());
        };
    }

    void loadGui() {
        if (!this.gui) {
            return;
        }
        this.guis.clear();
        this.getServer().getScheduler().cancelTasks(this);
        final File files = new File(TGS.instance.getDataFolder() + File.separator + "Menu");
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
                    Bukkit.getLogger().info("Loading Gui '" + fileEntry.getName().replace(".yml", "") + "'");
                    if (command.split(":").length > 1 && command.split(":")[1].contains("dynamic")) {
                        File templates = new File(TGS.instance.getDataFolder() + File.separator + "Templates" + File.separator + configuration.getString("templates", null) + ".yml");
                        if (!templates.exists()) {
                            templates.createNewFile();
                            log("File - " + configuration.getString("template", null) + ".yml not found");
                        } else
                            this.guis.put(command.split(":")[0], new Gui(command.split(":")[0], configuration, YamlConfiguration.loadConfiguration(templates)));
                    } else
                        this.guis.put(command, new Gui(command, configuration));
                    Bukkit.getLogger().info("Gui '" + fileEntry.getName().replace(".yml", "") + "' successfully uploaded");
                } catch (Exception ex) {
                    Bukkit.getLogger().info("Error loading GUI - " + fileEntry.getName().replace(".yml", ""));
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
        this.replaceItemOnJoin = this.getConfig().getBoolean("ReplaceItemOnJoin", false);
        this.itemManager = new ItemManager();
        new ItemListener();
    }

    public static void connect(final Player p, String where) {

        String type = where.split("_")[1];

        List<ServerModel> servers = new ArrayList<>();

        TGS.servers.sort(Comparator.comparing(ServerModel::getNowPlayer));

        for (final ServerModel s : TGS.servers)
            if (s.getName().contains(where.split("_")[0])
                    && (!s.getInStatus().equalsIgnoreCase("ingame")) &&
                    (!s.getInStatus().equalsIgnoreCase("starting")) &&
                    (!s.getInStatus().equalsIgnoreCase("offline")))
                servers.add(s);

        if (servers.size() < 1)
            p.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.wrongArgumentConnect")));

        switch (type) {
            case "random":
                Collections.shuffle(servers);
                break;

            case "max":
                Collections.reverse(servers);
                break;
        }

        if (servers.size() > 0) {

            if (nameServer.equalsIgnoreCase(servers.get(0).getName())) {
                p.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.areYouHere")) + "§a" + nameServer);
                return;
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(servers.get(0).getName());
            } catch (IOException e) {
                log(e.getMessage());
            }

            p.sendPluginMessage(TGS.instance, "BungeeCord", b.toByteArray());
        }
    }

    public static void registerListener(final Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, TGS.instance);
    }

    public static void log(final String message) {
        Logger.getLogger("Minecraft").info("[TowerGuiSystem] " + message);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        switch (commandLabel.split("_")[0]) {
            case "gui":
                if (args.length == 0) {
                    for (String temp : TGS.files.getMSG().getStringList("GUI.help"))
                        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', temp));
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("gui.reload")) {
                        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.reload.noPermissionss")));
                        return true;
                    }
                    this.gui = this.getConfig().getBoolean("Enable.Gui");
                    this.items = this.getConfig().getBoolean("Enable.Items");
                    this.loadGui();
                    this.loadItems();
                    sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.reload.loadComplete")));
                    return true;
                }

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("list")) {
                        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.listGui")));

                        final ByteArrayOutputStream b = new ByteArrayOutputStream();
                        final DataOutputStream out = new DataOutputStream(b);

                        try {
                            out.writeUTF(nameServer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        getServer().sendPluginMessage(plugin, "tgs:channel", b.toByteArray());

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
                            sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.guiNotFound")));
                            return true;
                        }
                        gui.open(player);
                        return true;
                    }
                }

                for (String temp : TGS.files.getMSG().getStringList("GUI.help"))
                    sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', temp));

                return true;

            case "connect":
                if (sender instanceof Player)
                    if (args.length > 0)
                        connect((Player) sender, args[0] + "_normal");
                    else
                        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.wrongArgumentConnect")));
                return true;

            case "maxconnect":
                if (sender instanceof Player)
                    if (args.length > 0)
                        connect((Player) sender, args[0] + "_max");
                    else
                        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.wrongArgumentConnect")));
                return true;

            default:
                sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', TGS.files.getMSG().getString("GUI.main.commandNotFound")));
                return true;
        }
    }

    public static int getOnline(String online) {
        int result = -1;
        List<String> usage = new ArrayList<>();

        for (ServerModel temp : servers)
            if (online.contains(temp.getName().split("-")[0])) {
                if (result == -1)
                    result = temp.getNowPlayer();
                else
                    result += temp.getNowPlayer();
                if (usage.stream().noneMatch(s -> s.contains(temp.getName().split("-")[0])))
                    usage.add(temp.getName().split("-")[0]);
            }

        for (ServerModel temp : lobbys)
            if (online.contains(temp.getName().split("-")[0]) && usage.stream().noneMatch(s -> s.contains(temp.getName().split("-")[0])))
                if (result == -1)
                    result = temp.getNowPlayer();
                else
                    result += temp.getNowPlayer();

        return result;
    }

    public static int getAllOnline() {
        int result = -1;
        List<String> usage = new ArrayList<>();

        for (ServerModel temp : servers) {
            if (result == -1)
                result = temp.getNowPlayer();
            else
                result += temp.getNowPlayer();
            if (usage.stream().noneMatch(s -> s.contains(temp.getName().split("-")[0])))
                usage.add(temp.getName().split("-")[0]);
        }

        for (ServerModel temp : lobbys)
            if (usage.stream().noneMatch(s -> s.contains(temp.getName().split("-")[0])))
                if (result == -1)
                    result = temp.getNowPlayer();
                else
                    result += temp.getNowPlayer();

        return result;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("tgs:channel")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        servers.clear();
        lobbys.clear();
        serversOnline.clear();
        lobbysOnline.clear();

        while (true) {
            try {
                String[] data = in.readUTF().split(":");

                if (data[0].equals("serverName")) {
                    nameServer = data[1];
                    return;
                }

                ServerModel temp = new ServerModel(data[1], data[2], data[3], data[4], Integer.parseInt(data[5]), Integer.parseInt(data[6]));

                if (data[0].equals("server"))
                    servers.add(temp);
                else
                    lobbys.add(temp);
            } catch (Exception e) {
                break;
            }
        }

        for (final ServerModel s : lobbys)
            lobbysOnline.put(s.getName(), s.getNowPlayer() + "/" + s.getMaxPlayers());

        for (int i = 0; i < servers.size(); i++) {
            ServerModel temp = servers.get(i);
            if (temp.getName().split("-").length > 1 && i == 0)
                serversOnline.put(temp.getName().split("-")[0], 0 + "/" + 0);
            else if (temp.getName().split("-").length == 1)
                serversOnline.put(temp.getName().split("-")[0], 0 + "/" + 0);
            int now = (serversOnline.get(temp.getName().split("-")[0]) != null ? Integer.parseInt(serversOnline.get(temp.getName().split("-")[0]).split("/")[0]) : 0) + temp.getNowPlayer();
            int max = (serversOnline.get(temp.getName().split("-")[0]) != null ? Integer.parseInt(serversOnline.get(temp.getName().split("-")[0]).split("/")[1]) : 0) + temp.getMaxPlayers();
            serversOnline.put(temp.getName().split("-")[0], now + "/" + max);
        }
    }

    @Override
    public void onEnable() {

        SpigotUpdater updater = new SpigotUpdater(this, 76667);
        try {
            if (updater.checkForUpdates())
                log("An update was found! New version: " + updater.getLatestVersion() + " download: " + updater.getResourceURL());
        } catch (Exception e) {
            log("Could not check for updates! Stacktrace:");
            e.printStackTrace();
        }

        isPlaceholder = this.getConfig().getBoolean("Enable.PlaceHolderApi");

        if (isPlaceholder)
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceHolderExpansion().register();
                log("PlaceHolderExpansion - registered");
            } else
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");

        if (this.getConfig().getBoolean("Enable.HologramsDisplay"))
            if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
                hologramsDisplay = new HologramsDisplay(this);
                hologramsDisplay.registerPlaceholderPluginAll(this.getConfig().getStringList("Data.HologramsDisplay"));
            } else
                throw new RuntimeException("Could not find HolographicDisplays!! Plugin can not work without it!");

        this.saveDefaultConfig();
        TGS.plugin = this;
        TGS.instance = this;
        this.guis = new HashMap<>();

        updateTime = this.getConfig().getLong("General.updateInterval");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Регистрируем канал для получения серваков
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "tgs:channel");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "tgs:channel", this);

        this.gui = this.getConfig().getBoolean("Enable.Gui");
        this.items = this.getConfig().getBoolean("Enable.Items");

        this.getCommand("gui").setExecutor(this);
        this.getCommand("connect").setExecutor(this);

        //Инициализируем файл с сообщениями
        files = new Files(this);
        try {
            files.createMessages();
        } catch (Exception e){
            log("Error load files Stacktrace:");
            e.printStackTrace();
        }

        loadItems();
        loadGui();

        this.staticRunnable();
        this.startUpdate();
    }

    public void setCurrentServer() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Player player = (Player) Bukkit.getOnlinePlayers().toArray()[0];
        out.writeUTF(player.getName());
        player.sendPluginMessage(this, "tgs:channel", out.toByteArray());
    }

    static {
        TGS.servers = new ArrayList<>();
        TGS.nameServer = null;
        TGS.lobbys = new ArrayList<>();
        TGS.serversOnline = new HashMap<>();
        TGS.lobbysOnline = new HashMap<>();
        TGS.prefix = "§6TGS §8» §7";
    }

    @Override
    public void onDisable() {
        hologramsDisplay.unregisterPlaceholdersPlugin();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "tgs:channel");
    }
}
