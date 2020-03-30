package com.towercraft.items;

import com.towercraft.TowerGuiSystem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {
    FileConfiguration config;
    public Map<String, Map<Integer, Item>> items;

    public ItemManager() {
        this.load();
    }

    public void load() {
        this.items = new HashMap<>();
        final File files = new File(TowerGuiSystem.instance.getDataFolder() + File.separator + "Items");

        //TODO Правильное создание файлов если их нет

        if (!files.exists()) {
            files.mkdir();
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(TowerGuiSystem.instance.getResource("Items/items_ru.yml")));
            try {
                this.config.save(files);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (final File fileEntry : files.listFiles()) {

            this.config = YamlConfiguration.loadConfiguration(fileEntry);

            Map<Integer, Item> temp = new HashMap<>();

            for (final String name : this.config.getConfigurationSection("JoinItems").getKeys(false)) {
                try {
                    final String id = this.config.getString("JoinItems." + name + ".id", null);
                    if (id == null) {
                        throw new Exception("Invalid 'id' item");
                    }
                    if (Material.getMaterial(id.split(":")[0]) == null) {
                        throw new Exception("Invalid 'id' item " + name);
                    }
                    int slot = this.config.getInt("JoinItems." + name + ".slot", 0);
                    if (slot <= 0 || slot >= 40) {
                        throw new Exception("Invalid 'slot' item " + name);
                    }
                    --slot;
                    if (temp.get(slot) != null) {
                        throw new Exception("Item already exists with this slot!");
                    }
                    final String displayName = this.config.getString("JoinItems." + name + ".name", null);
                    final List<String> lore = this.config.getStringList("JoinItems." + name + ".lore");
                    final String command = this.config.getString("JoinItems." + name + ".command", null);
                    final int cooldown = this.config.getInt("JoinItems." + name + ".cooldown", 0);
                    final int amount = this.config.getInt("JoinItems." + name + ".amount", 1);
                    if (amount <= 0 || amount > 64) {
                        throw new Exception("Invalid 'amount' item " + name);
                    }
                    final boolean drop = this.config.getBoolean("JoinItems." + name + ".drop", false);
                    final boolean move = this.config.getBoolean("JoinItems." + name + ".move", false);
                    final Item item = new Item(name, id, slot, displayName, lore, command, amount, drop, move, cooldown);
                    temp.put(slot, item);
                    TowerGuiSystem.log("[TGSItems] Item '" + name + "' successfully uploaded");
                } catch (Exception ex2) {
                    TowerGuiSystem.log("[TGSItems] Error loading item '" + name + "'. Error - " + ex2.getMessage());
                }

                this.items.put(fileEntry.getName().split("_")[1].replace(".yml", ""), temp);
            }
        }
    }

    public Item getItem(String language, final ItemStack item) {
        if(this.items.get(language) == null)
            language = TowerGuiSystem.defaultLanguage;
        for (Map.Entry<Integer, Item> temp : this.items.get(language).entrySet()) {
            if (temp.getValue().getItemStack().equals(item)) {
                return temp.getValue();
            }
        }
        return null;
    }
}