package com.towercraft;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.google.common.collect.Ordering;
import com.towercraft.gui.Gui;
import com.towercraft.items.ItemListener;
import com.towercraft.items.ItemManager;
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
    public static Collection<ServerObject> servers;
    public static Collection<ServerObject> lobbys;
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
            servers.clear();
            lobbys.clear();
            for (String group : instance.getConfig().getConfigurationSection("general").getStringList("name_group")) {
                if (TimoCloudAPI.getUniversalAPI() != null)
                    if (TimoCloudAPI.getUniversalAPI().getServerGroup(group.split(":")[0]) != null) {
                        if (group.split(":").length > 1) {
                            lobbys.addAll(getOnlineServers(group.split(":")[0]));
                            for (final ServerObject s : getOnlineServers(group.split(":")[0]))
                                TowerGuiSystem.lobbysOnline.put(s.getName(), s.getOnlinePlayerCount() + "/" + s.getMaxPlayerCount());
                        } else {
                            servers.addAll(getOnlineServers(group));
                            for (final ServerObject s : getOnlineServers(group)) {
                                if (s.getName().split("-").length > 1 && s.getName().split("-")[1].equals("1"))
                                    TowerGuiSystem.serversOnline.put(s.getName().split("-")[0], 0 + "/" + 0);
                                else if (s.getName().split("-").length == 1)
                                    TowerGuiSystem.serversOnline.put(s.getName().split("-")[0], 0 + "/" + 0);
                                int now = (TowerGuiSystem.serversOnline.get(s.getName().split("-")[0]) != null ? Integer.parseInt(TowerGuiSystem.serversOnline.get(s.getName().split("-")[0]).split("/")[0]) : 0) + s.getOnlinePlayerCount();
                                int max = (TowerGuiSystem.serversOnline.get(s.getName().split("-")[0]) != null ? Integer.parseInt(TowerGuiSystem.serversOnline.get(s.getName().split("-")[0]).split("/")[1]) : 0) + s.getMaxPlayerCount();
                                TowerGuiSystem.serversOnline.put(s.getName().split("-")[0], now + "/" + max);
                            }
                        }
                    } else if (group.split(":").length == 1)
                        TowerGuiSystem.serversOnline.put(group, 0 + "/" + 0);
            }
        };
    }

    List<ServerObject> getOnlineServers(String nameGroup) {
        List<ServerObject> result = new ArrayList<>(TimoCloudAPI.getUniversalAPI().getServerGroup(nameGroup).getServers());
        result.sort(Ordering.usingToString());
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
                    Bukkit.getLogger().info("\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u044e Gui '" + fileEntry.getName().replace(".yml", "") + "'");
                    if (command.contains("lobby")) {
                        File templates = new File(TowerGuiSystem.instance.getDataFolder() + File.separator + "Templates" + File.separator + configuration.getString("templates", null) + ".yml");
                        if (!templates.exists()) {
                            templates.mkdir();
                            log("Файл - " + configuration.getString("template", null) + ".yml не найден");
                        }
                        this.guis.put(command, new Gui(command, configuration, YamlConfiguration.loadConfiguration(templates)));
                    } else
                        this.guis.put(command, new Gui(command, configuration));
                    Bukkit.getLogger().info("Gui '" + fileEntry.getName().replace(".yml", "") + "' \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u043e");
                } catch (Exception ex) {
                    Bukkit.getLogger().info("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0435 GUI - " + fileEntry.getName().replace(".yml", ""));
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

    public static void connect(final Player p, final String where) {

        log(where);

        List<ServerObject> random = new ArrayList<>();

        for (final ServerObject s : servers) {
            if (s.getName().contains(where)) {
                random.add(s);
            }
        }

        if (random.size() < 1)
            p.sendMessage(getPrefix() + "Не найден сервер");

        Collections.shuffle(random);

        if (random.size() > 0) {

            if (p.getServer().getName().equalsIgnoreCase(random.get(0).getName())) {
                p.sendMessage(Arrays.toString(new ComponentBuilder("Вы уже находитесь на - " + where).color(ChatColor.RED).create()));
                return;
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            try {
                out.writeUTF("Connect");
                out.writeUTF(random.get(0).getName());
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
        switch (commandLabel) {
            case "gui":
                if (args.length == 0) {
                    sender.sendMessage(getPrefix() + "\u041e\u0442\u043a\u0440\u044b\u0442\u044c Gui - §c/gui open [\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435]");
                    sender.sendMessage(getPrefix() + "\u0421\u043f\u0438\u0441\u043e\u043a Gui - §c/gui list");
                    return true;
                }
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("list")) {
                        sender.sendMessage(getPrefix() + "\u0421\u043f\u0438\u0441\u043e\u043a Gui:");
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
                            sender.sendMessage(getPrefix() + "§cGui \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e!");
                            return true;
                        }
                        gui.open(player);
                        return true;
                    }
                }
                sender.sendMessage(getPrefix() + "\u041e\u0442\u043a\u0440\u044b\u0442\u044c Gui - §c/gui open [\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435]");
                sender.sendMessage(getPrefix() + "\u0421\u043f\u0438\u0441\u043e\u043a Gui - §c/gui list");
                return true;

            case "connect":
                if (sender instanceof Player)
                    if (args.length > 0)
                        connect((Player) sender, args[0]);
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

        System.out.println("Enabling, please wait...");
        this.saveDefaultConfig();
        TowerGuiSystem.plugin = this;
        TowerGuiSystem.instance = this;
        this.guis = new HashMap<>();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.gui = this.getConfig().getBoolean("Enable.Gui");
        this.items = this.getConfig().getBoolean("Enable.Items");
        this.source = this.getConfig().getString("source");
        this.loadItems();
        this.staticRunnable();
        this.startUpdate();
        this.getCommand("gui").setExecutor(this);
        this.getCommand("connect").setExecutor(this);
        loadGui();
    }

    static {
        TowerGuiSystem.servers = new ArrayList<>();
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
