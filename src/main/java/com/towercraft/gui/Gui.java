package com.towercraft.gui;

import com.towercraft.TowerGuiSystem;
import com.towercraft.utils.ServerModel;
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
import java.util.Map;
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
            TowerGuiSystem.log("Ошибка при загрузки GUI '" + this.name + "'. template = null");
            return;
        }

        final String gname = lobby.getString("name", this.name);

        if (gname == null) {
            TowerGuiSystem.log("Имя GUI - null");
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



                int countName = 0;

                List<ServerModel> filterLobby = TowerGuiSystem.lobbys.stream().filter(serverModel -> serverModel.getName().contains(lobby.getString("item.nameserver").replace(lobby.getString("item.nameserver").substring(0,2), ""))).collect(Collectors.toList());
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

                        try {
                            //В зависимости от того сколько игроков подгружаем нужный id предмета
                            String id = lobby.getStringList("items").get(0);

                            if (Integer.parseInt(TowerGuiSystem.lobbysOnline.get(lobbyGet.getName()).split("/")[0]) >=
                                    lobby.getIntegerList("item.count").get(1))
                                id = lobby.getStringList("items").get(1);

                            if (Integer.parseInt(TowerGuiSystem.lobbysOnline.get(lobbyGet.getName()).split("/")[0]) >=
                                    lobby.getIntegerList("item.count").get(2))
                                id = lobby.getStringList("items").get(2);

                            final String name = lobby.getString("item.nameserver").replace("&", "§") + "-" + countName;
                            final List<String> lore_config = lobby.getStringList("item.lore");
                            final List<String> lore_result = new ArrayList<>();

                            for (String temp : lore_config) {
                                if (TowerGuiSystem.nameServer.equalsIgnoreCase(lobbyGet.getName()))
                                    lore_result.add(temp.replace("%place%", "Вы находитесь здесь"));
                                else
                                    lore_result.add(temp.replace("%place%", ""));
                            }

                            final String command = "server:" + filterLobby.get(countName - 1).getName();
                            Gui.server = name;

                            String online = "";
                            for (Map.Entry<String, String> entry : TowerGuiSystem.lobbysOnline.entrySet())
                                if (entry.getKey().contains(lobbyGet.getName()))
                                    online = entry.getValue();

                            final GuiItem guiItem = new GuiItem(Gui.this, id, Integer.parseInt(online.split("/")[0]), name, lore_result, i, command, new ArrayList<>(), Gui.server);

                            final ItemMeta meta = guiItem.getItemStack().getItemMeta();
                            final List<String> nlore = new ArrayList<>();

                            for (final String l : guiItem.lore) {
                                final String line = l.replace("%so%", "" + online).replace("%map%", lobbyGet.getMap()).replace("%status%", lobbyGet.getInStatus());
                                nlore.add(line);
                            }

                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                            meta.setLore(nlore);
                            guiItem.item.setItemMeta(meta);
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
            }
        }.runTaskTimer(TowerGuiSystem.instance, 40L, 20L);
    }

    void load(boolean statics) {
        if (statics) {
            this.displayName = this.config.getString("name", this.name).replace("&", "§");
            this.inventory = Bukkit.createInventory(null, this.config.getInt("rows") * 9, this.displayName);
        }
        if (this.config == null) {
            TowerGuiSystem.log("Ошибка при загрзуки GUI '" + this.name + "'. config = null");
            return;
        }
        if (this.config.getConfigurationSection("Items") == null) {
            TowerGuiSystem.log("Ошибка при загрзуки GUI '" + this.name + "'. Items не найдена");
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
                TowerGuiSystem.log("\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u044e \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044e \u0434\u043b\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 " + itemName);
                animation = new ArrayList<>();
                for (final String string : this.config.getConfigurationSection("Items." + itemName + ".animation").getKeys(false)) {
                    final List<String> list = new ArrayList<>();
                    for (final String str : this.config.getStringList("Items." + itemName + ".animation." + string)) {
                        list.add(str.replace("&", "§"));
                    }
                    animation.add(list);
                }
                TowerGuiSystem.log("\u041b\u0438\u0441\u0442\u043e\u0432 \u0432 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 - " + animation.size());
                final GuiItem guiItem = new GuiItem(this, id, amount, name, lore, slot, command, animation, Gui.server);
                this.items.put(slot, guiItem);
                this.startSheduler(guiItem);
                for (final int s : this.items.keySet()) {
                    try {
                        this.inventory.setItem(s, this.items.get(s).getItemStack());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        TowerGuiSystem.log("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0441\u043b\u043e\u0442\u0435 '" + s + "' \u0432\u044b\u0437\u0432\u0430\u043b \u043e\u0448\u0438\u0431\u043a\u0443!");
                    }
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
                TowerGuiSystem.log("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 '" + itemName + "' \u0432 GUI '" + this.name + "'");
            }
        }
    }

    void startSheduler(final GuiItem item) {
        new BukkitRunnable() {
            public void run() {
                final ItemMeta meta = item.getItemStack().getItemMeta();
                final List<String> nlore = new ArrayList<>();
                String online = "";
                for (Map.Entry<String, String> entry : TowerGuiSystem.serversOnline.entrySet())
                    if (entry.getKey().contains(item.server))
                        online = Integer.parseInt(entry.getValue().split("/")[1]) == 0 ? "§cOffline" : entry.getValue();

                for (final String l : item.lore) {
                    final String line = l.replace("%so%", "" + online);
                    nlore.add(line);
                }

                if (!item.iterator.hasNext()) {
                    List<List<String>> temp = new ArrayList<>();

                    for (int i = 0; i < item.animation.size(); i++) {
                        List<String> replacement = new ArrayList<>();
                        for (int j = 0; j < item.animation.get(i).size(); j++) {
                            replacement.add(item.animation.get(i).get(j).replace("%so%", "" + online));
                        }
                        temp.add(replacement);
                    }

                    item.iterator = temp.iterator();
                }

                nlore.addAll(item.iterator.next());

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                meta.setLore(nlore);
                item.item.setItemMeta(meta);
                Gui.this.inventory.setItem(item.getSlot(), item.getItemStack());
            }
        }.runTaskTimer(TowerGuiSystem.instance, 40L, TowerGuiSystem.instance.getConfig().getInt("AnimationTime"));
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
