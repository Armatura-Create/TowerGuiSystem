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
                    throw new Exception("\u041d\u0435\u0432\u0435\u0440\u043d\u043e \u0443\u043a\u0430\u0437\u0430\u043d 'id' \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430");
                }
                if (Material.getMaterial(id.split(":")[0]) == null) {
                    throw new Exception("\u041d\u0435\u0432\u0435\u0440\u043d\u043e \u0443\u043a\u0430\u0437\u0430\u043d 'id' \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 " + name);
                }
                int slot = this.config.getInt("JoinItems." + name + ".slot", 0);
                if (slot <= 0 || slot >= 40) {
                    throw new Exception("\u041d\u0435\u0432\u0435\u0440\u043d\u043e \u0443\u043a\u0430\u0437\u0430\u043d 'slot' \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 " + name);
                }
                --slot;
                if (this.items.get(slot) != null) {
                    throw new Exception("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0441 \u0434\u0430\u043d\u043d\u044b\u043c \u0441\u043b\u043e\u0442\u043e\u043c \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!");
                }
                final String displayName = this.config.getString("JoinItems." + name + ".name", null);
                final List<String> lore = this.config.getStringList("JoinItems." + name + ".lore");
                final String command = this.config.getString("JoinItems." + name + ".command", null);
                final int cooldown = this.config.getInt("JoinItems." + name + ".cooldown", 0);
                final int amount = this.config.getInt("JoinItems." + name + ".amount", 1);
                if (amount <= 0 || amount > 64) {
                    throw new Exception("\u041d\u0435\u0432\u0435\u0440\u043d\u043e \u0443\u043a\u0430\u0437\u0430\u043d\u043e 'amount' \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 " + name);
                }
                final boolean drop = this.config.getBoolean("JoinItems." + name + ".drop", false);
                final boolean move = this.config.getBoolean("JoinItems." + name + ".move", false);
                final Item item = new Item(name, id, slot, displayName, lore, command, amount, drop, move, cooldown);
                this.items.put(slot, item);
                TowerGuiSystem.log("[TGSItems] \u041f\u0440\u0435\u0434\u043c\u0435\u0442 '" + name + "' \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d");
            } catch (Exception ex2) {
                TowerGuiSystem.log("[TGSItems] \u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 '" + name + "'. \u041e\u0448\u0438\u0431\u043a\u0430 - " + ex2.getMessage());
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