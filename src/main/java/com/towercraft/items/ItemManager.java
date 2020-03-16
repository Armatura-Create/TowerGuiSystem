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
    public Map<Integer, Item> items;

    public ItemManager() {
        this.load();
    }

    public void load() {
        this.items = new HashMap<>();
        final File file = new File(TowerGuiSystem.instance.getDataFolder(), "items.yml");
        if (!file.exists()) {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(TowerGuiSystem.instance.getResource("items.yml")));
            try {
                this.config.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        for (final String name : this.config.getConfigurationSection("JoinItems").getKeys(false)) {
            try {
                final String id = this.config.getString("JoinItems." + name + ".id", null);
                if (id == null) {
                    throw new Exception("Неверно указан 'id' предмета");
                }
                if (Material.getMaterial(id.split(":")[0]) == null) {
                    throw new Exception("Неверно указан 'id' предмета " + name);
                }
                int slot = this.config.getInt("JoinItems." + name + ".slot", 0);
                if (slot <= 0 || slot >= 40) {
                    throw new Exception("Неверно указан 'slot' предмета " + name);
                }
                --slot;
                if (this.items.get(slot) != null) {
                    throw new Exception("Предмет с данным слотом уже существует!");
                }
                final String displayName = this.config.getString("JoinItems." + name + ".name", null);
                final List<String> lore = this.config.getStringList("JoinItems." + name + ".lore");
                final String command = this.config.getString("JoinItems." + name + ".command", null);
                final int cooldown = this.config.getInt("JoinItems." + name + ".cooldown", 0);
                final int amount = this.config.getInt("JoinItems." + name + ".amount", 1);
                if (amount <= 0 || amount > 64) {
                    throw new Exception("Неверно указано 'amount' предмета " + name);
                }
                final boolean drop = this.config.getBoolean("JoinItems." + name + ".drop", false);
                final boolean move = this.config.getBoolean("JoinItems." + name + ".move", false);
                final Item item = new Item(name, id, slot, displayName, lore, command, amount, drop, move, cooldown);
                this.items.put(slot, item);
                TowerGuiSystem.log("[TGSItems] Предмет '" + name + "' успешно загружен");
            } catch (Exception ex2) {
                TowerGuiSystem.log("[TGSItems] Ошибка при загрузке предмета '" + name + "'. Ошибка - " + ex2.getMessage());
            }
        }
    }

    public Item getItem(final ItemStack item) {
        for (final int i : this.items.keySet()) {
            if (this.items.get(i).getItemStack().equals(item)) {
                return this.items.get(i);
            }
        }
        return null;
    }
}