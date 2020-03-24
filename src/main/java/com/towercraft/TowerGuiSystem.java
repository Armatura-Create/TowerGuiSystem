package com.towercraft;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.towercraft.gui.Gui;
import com.towercraft.items.ItemListener;
import com.towercraft.items.ItemManager;
import com.towercraft.utils.ServerModel;
import org.bukkit.Bukkit;
import org.bukkit.Server;
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

public final class TowerGuiSystem extends JavaPlugin implements CommandExecutor, PluginMessageListener {

    public static TowerGuiSystem instance;
    private HashMap<String, Gui> guis;
    public ItemManager itemManager;
    public static TowerGuiSystem plugin;
    boolean gui;
    boolean items;
    public boolean clearOnJoin;
    public static boolean isUpdate = false;
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
            while (true) {
                try {
                    while (true) {
                        Thread.sleep(5000L);
                        if (Bukkit.getOnlinePlayers().toArray().length > 0 && nameServer == null)
                            setCurrentServer();
                        if (nameServer != null && !isUpdate)
                            TowerGuiSystem.this.mainRunnable.run();
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
                    Bukkit.getLogger().info("Ошибка при загрузки GUI - " + fileEntry.getName().replace(".yml", ""));
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
                    && (!s.getInStatus().equalsIgnoreCase("ingame")))
                servers.add(s);

        if (servers.size() < 1) {
            String result;
            if (Integer.parseInt(getMinecraftVersion(TowerGuiSystem.plugin.getServer()).replace(".", "-").split("-")[1]) >= 12)
                switch (p.getLocale().toLowerCase()) {
                    case "ru_ru":
                        result = "Не найден сервер";
                        break;
                    default:
                        result = "Server not found";
                }
            else
                result = "Не найден сервер";
            p.sendMessage(getPrefix() + result);
        }

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
                String result;

                if (Integer.parseInt(getMinecraftVersion(TowerGuiSystem.plugin.getServer()).replace(".", "-").split("-")[1]) >= 12)
                    switch (p.getLocale().toLowerCase()) {
                        case "ru_ru":
                            result = "Вы уже находитесь в - ";
                            break;
                        default:
                            result = "You are already in - ";
                    }
                else
                    result = "Вы уже находитесь в - ";
                p.sendMessage(getPrefix() + result + "§a" + nameServer);
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

    private static String getMinecraftVersion(Server server) {
        // Same substring as the one bStats uses, so should be safe
        String version = server.getVersion();
        int start = version.indexOf("MC: ") + 4;
        int end = version.length() - 1;
        return version.substring(start, end);
    }

    @Override
    public void onEnable() {

        //TODO Добавить проверку на все нужные плагины

        this.saveDefaultConfig();
        TowerGuiSystem.plugin = this;
        TowerGuiSystem.instance = this;
        this.guis = new HashMap<>();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Регистрируем канал для получения серваков
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "tgs:channel");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "tgs:channel", this);

        this.gui = this.getConfig().getBoolean("Enable.Gui");
        this.items = this.getConfig().getBoolean("Enable.Items");

        loadItems();
        this.getCommand("gui").setExecutor(this);
        this.getCommand("connect").setExecutor(this);
        loadGui();

        this.staticRunnable();
        this.startUpdate();
    }
    public void setCurrentServer(){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Player player = (Player)Bukkit.getOnlinePlayers().toArray()[0];
        out.writeUTF(player.getName());
        player.sendPluginMessage(this, "tgs:channel", out.toByteArray());
    }


    static {
        TowerGuiSystem.servers = new ArrayList<>();
        TowerGuiSystem.nameServer = null;
        TowerGuiSystem.lobbys = new ArrayList<>();
        TowerGuiSystem.serversOnline = new HashMap<>();
        TowerGuiSystem.lobbysOnline = new HashMap<>();
        TowerGuiSystem.prefix = "§6TGS §8» §7";
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "tgs:channel");
    }
}
