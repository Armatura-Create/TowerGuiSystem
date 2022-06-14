package me.towercraft.service;

import me.towercraft.TGS;
import me.towercraft.plugin.ioc.annotations.Autowire;
import me.towercraft.plugin.ioc.annotations.Component;
import me.towercraft.plugin.ioc.annotations.PostConstruct;
import me.towercraft.ui.items.Item;
import me.towercraft.utils.TGSLogger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ItemService {

    @Autowire
    private TGS plugin;
    @Autowire
    private TGSLogger tgsLogger;
    private FileConfiguration config;
    private Map<Integer, Item> items;

    @PostConstruct
    public void load() {
        this.items = new ConcurrentHashMap<>();
        final File files = new File(plugin.getDataFolder() + File.separator + "Items");

        if (!files.exists()) {
            files.mkdir();
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("Items/items.yml"))));
            try {
                this.config.save(files);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (final File fileEntry : files.listFiles()) {

            this.config = YamlConfiguration.loadConfiguration(fileEntry);

            Map<Integer, Item> temp = new HashMap<>();

            for (String type : this.config.getConfigurationSection("JoinItems").getKeys(false)) {
                try {
                    String id = this.config.getString("JoinItems." + type + ".id", null);
                    if (id == null) {
                        throw new Exception("Invalid 'id' item");
                    }

                    if (Material.getMaterial(id.split(":")[0]) == null) {
                        throw new Exception("Invalid 'id' item " + type);
                    }

                    int slot = this.config.getInt("JoinItems." + type + ".slot", 0);
                    if (slot <= 0 || slot >= 40) {
                        throw new Exception("Invalid 'slot' item " + type);
                    }

                    --slot;

                    if (temp.get(slot) != null) {
                        throw new Exception("Item already exists with this slot!");
                    }

                    String displayNameItem = this.config.getString("JoinItems." + type + ".name", null);
                    List<String> descriptions = this.config.getStringList("JoinItems." + type + ".lore");
                    String command = this.config.getString("JoinItems." + type + ".command", null);

                    int cooldown = this.config.getInt("JoinItems." + type + ".cooldown", 0);
                    int amount = this.config.getInt("JoinItems." + type + ".amount", 1);
                    if (amount <= 0 || amount > 64) {
                        throw new Exception("Invalid 'amount' item " + type);
                    }

                    boolean drop = this.config.getBoolean("JoinItems." + type + ".drop", false);
                    boolean move = this.config.getBoolean("JoinItems." + type + ".move", false);

                    temp.put(slot, new Item(
                            id,
                            slot,
                            displayNameItem,
                            descriptions,
                            command, amount,
                            drop,
                            move,
                            cooldown
                    ));

                    tgsLogger.log("[TGSItems] Item '" + type + "' successfully uploaded");
                } catch (Exception ex2) {
                    tgsLogger.log("[TGSItems] Error loading item '" + type + "'. Error - " + ex2.getMessage());
                }

                this.items = temp;
            }
        }
    }

    public Item getItem(ItemStack item) {
        for (Map.Entry<Integer, Item> temp : this.items.entrySet()) {
            if (temp.getValue().getItemStack().equals(item)) {
                return temp.getValue();
            }
        }
        return null;
    }

    public Map<Integer, Item> getItems() {
        return items;
    }
}