package me.towercraft.gui;

import me.towercraft.TGS;
import me.towercraft.utils.ServerModel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Gui {
    private String name;
    private String displayName;
    private HashMap<Integer, GuiItem> items;
    private Inventory inventory;
    public static String server;

    FileConfiguration config;

    public Gui(final String name, final FileConfiguration config) {
        this.config = config;
        this.name = name.replace("&", "§");
        this.items = new HashMap<>();

        this.load(true);
        new GuiListener(this);
    }

    public Gui(final String name, final FileConfiguration lobby, final FileConfiguration template) {
        this.config = template;
        this.name = name.replace("&", "§");
        this.items = new HashMap<>();

        if (template == null) {
            TGS.log("Error loading GUI '" + this.name + "'. template = null");
            return;
        }

        final String gname = lobby.getString("name", this.name);

        if (gname == null) {
            TGS.log("Name GUI - null");
            return;
        }

        this.displayName = gname.replace("&", "§");

        this.inventory = Bukkit.createInventory(null, lobby.getInt("rows") * 9, this.displayName);

        this.load(false);
        startDynamic(lobby);
        new GuiListener(this);
    }

    private void startDynamic(FileConfiguration lobby) {
        new BukkitRunnable() {
            @Override
            public void run() {

                TGS.isUpdate = true;

                int countName = 0;

                List<ServerModel> filterLobby = TGS.lobbys.stream().filter(serverModel -> serverModel.getName().contains(lobby.getString("item.nameserver").replace(lobby.getString("item.nameserver").substring(0, 2), ""))).collect(Collectors.toList());
                int plus = 0;
                for (int i = 10; i < (lobby.getInt("rows") - 1) * 9; i++) {

                    if (i % 9 == 0 || i == 17 || i == 26 || i == 35 || i == 44) {
                        plus++;
                        continue;
                    }

                    items.remove(i);
                    inventory.clear(i);
                    countName++;

                    if (i < filterLobby.size() + 10 + plus) {

                        ServerModel lobbyGet = filterLobby.get(countName - 1);

                        if (TGS.nameServer != null && lobbyGet != null)
                            try {
                                //В зависимости от того сколько игроков подгружаем нужный id предмета
                                String id = lobby.getStringList("items").get(0);

                                if (Integer.parseInt(TGS.lobbysOnline.get(lobbyGet.getName()).split("/")[0]) >=
                                        lobby.getIntegerList("item.count").get(1))
                                    id = lobby.getStringList("items").get(1);

                                if (Integer.parseInt(TGS.lobbysOnline.get(lobbyGet.getName()).split("/")[0]) >=
                                        lobby.getIntegerList("item.count").get(2))
                                    id = lobby.getStringList("items").get(2);

                                if (lobbyGet.getInStatus().equals("starting") || lobbyGet.getInStatus().equals("offline"))
                                    id = lobby.getStringList("items").get(3);

                                final String name = lobby.getString("item.nameserver").replace("&", "§") + "-" + countName;
                                final List<String> lore_config = lobby.getStringList("item.lore");
                                final List<String> lore_result = new ArrayList<>();

                                for (String temp : lore_config) {
                                    if (TGS.nameServer.equalsIgnoreCase(lobbyGet.getName())) {
                                        lore_result.add(temp.replace("%place%", "Вы находитесь здесь"));
                                        id = lobby.getStringList("items").get(4);
                                    }
                                    else
                                        lore_result.add(temp.replace("%place%", ""));
                                }

                                final String command = "server:" + filterLobby.get(countName - 1).getName();
                                Gui.server = name;

                                final GuiItem guiItem = new GuiItem(Gui.this, id, Integer.parseInt(TGS.lobbysOnline.get(lobbyGet.getName()).split("/")[0]), name, lore_result, i, command, new ArrayList<>(), filterLobby.get(countName - 1), server);

                                final ItemMeta meta = guiItem.getItemStack().getItemMeta();
                                final List<String> nlore = new ArrayList<>();

                                for (final String l : guiItem.getLore()) {
                                    final String line = l.replace("%so%", "" + TGS.lobbysOnline.get(lobbyGet.getName())).replace("%map%", lobbyGet.getMap()).replace("%status%", lobbyGet.getInStatus().replace("ONLINE", "Ожидание"));
                                    nlore.add(line);
                                }

                                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                                meta.setLore(nlore);
                                guiItem.getItem().setItemMeta(meta);
                                items.put(i, guiItem);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }

                for (final int s : items.keySet()) {
                    try {
                        inventory.setItem(s, items.get(s).getItemStack());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                TGS.isUpdate = false;
            }
        }.runTaskTimer(TGS.instance, 40L, 20L);
    }

    void load(boolean statics) {
        if (statics) {
            this.displayName = this.config.getString("name", this.name).replace("&", "§");
            this.inventory = Bukkit.createInventory(null, this.config.getInt("rows") * 9, this.displayName);
        }
        if (this.config == null) {
            TGS.log("Ошибка при загрзуки GUI '" + this.name + "'. config = null");
            return;
        }
        if (this.config.getConfigurationSection("Items") == null) {
            TGS.log("Ошибка при загрзуки GUI '" + this.name + "'. Items не найдена");
            return;
        }
        for (final String itemName : this.config.getConfigurationSection("Items").getKeys(false)) {
            try {
                final int slot = this.config.getInt("Items." + itemName + ".slot") - 1;
                final String id = this.config.getString("Items." + itemName + ".id");
                final String name = this.config.getString("Items." + itemName + ".name").replace("&", "§");
                final List<String> lore = this.config.getStringList("Items." + itemName + ".lore");
                final String command = this.config.getString("Items." + itemName + ".command");
                final int amount = this.config.getInt("Items." + itemName + ".amount");
                Gui.server = this.config.getString("Items." + itemName + ".server");
                List<List<String>> animation;
                TGS.log("Loading animation from item " + itemName);
                animation = new ArrayList<>();
                for (final String string : this.config.getConfigurationSection("Items." + itemName + ".animation").getKeys(false)) {
                    final List<String> list = new ArrayList<>();
                    for (final String str : this.config.getStringList("Items." + itemName + ".animation." + string)) {
                        list.add(str.replace("&", "§"));
                    }
                    animation.add(list);
                }
                TGS.log("List of animation - " + animation.size());
                final GuiItem guiItem = new GuiItem(this, id, amount, name, lore, slot, command, animation, TGS.servers.stream().filter(serverModel -> serverModel.getName().equals(server)).findFirst().orElse(null), server);
                this.items.put(slot, guiItem);
                this.startSheduler(guiItem);
                for (final int s : this.items.keySet()) {
                    try {
                        this.inventory.setItem(s, this.items.get(s).getItemStack());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        TGS.log("Item in slot '" + s + "' throw exception!");
                    }
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
                TGS.log("Error from loading item '" + itemName + "' in GUI '" + this.name + "'");
            }
        }
    }

    void startSheduler(final GuiItem item) {
        new BukkitRunnable() {
            public void run() {
                final ItemMeta meta = item.getItemStack().getItemMeta();
                final List<String> nlore = new ArrayList<>();
                String online;

                online = TGS.serversOnline.get(item.getServer()) == null ? "§cOffline" : TGS.serversOnline.get(item.getServer());

                for (final String l : item.getLore()) {
                    final String line = l.replace("%so%", online);
                    nlore.add(line);
                }

                if (!item.iterator.hasNext()) {
                    List<List<String>> temp = new ArrayList<>();

                    for (int i = 0; i < item.getAnimation().size(); i++) {
                        List<String> replacement = new ArrayList<>();
                        for (int j = 0; j < item.getAnimation().get(i).size(); j++) {
                            replacement.add(item.getAnimation().get(i).get(j).replace("%so%", "" + online).replace("%sa%", "" + TGS.lobbys.stream().filter(serverModel -> serverModel.getName().split("-")[0].equalsIgnoreCase(item.getServer())).count()));
                        }
                        temp.add(replacement);
                    }

                    item.iterator = temp.iterator();
                }

                nlore.addAll(item.iterator.next());

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                meta.setLore(nlore);
                item.getItem().setItemMeta(meta);
                Gui.this.inventory.setItem(item.getSlot(), item.getItemStack());
            }
        }.runTaskTimer(TGS.instance, 40L, TGS.instance.getConfig().getInt("AnimationTime"));
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    public GuiItem getItem(final int slot) {
        return this.items.get(slot);
    }

    public void open(final Player player) {
        player.openInventory(this.inventory);
    }
}
