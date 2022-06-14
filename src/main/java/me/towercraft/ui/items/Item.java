package me.towercraft.ui.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Item {
    private ItemStack item;
    private int slot;
    private String command;
    private boolean drop;
    private boolean move;
    private int cooldown;
    HashMap<String, Integer> cooldowns;

    public Item(String id,
                int slot,
                String displayName,
                List<String> lore,
                String command,
                int amount,
                boolean drop,
                boolean move,
                int cooldown) {
        final String[] ids = id.split(":");
        if (ids.length == 1) {
            this.item = new ItemStack(Material.getMaterial(ids[0]), amount);
        } else {
            this.item = new ItemStack(Material.getMaterial(ids[0]), amount, Short.parseShort(ids[1]));
        }
        this.slot = slot;
        final ItemMeta meta = this.item.getItemMeta();
        if (displayName != null) {
            meta.setDisplayName(displayName.replace("&", "§"));
        }
        if (lore != null) {
            final List<String> slore = new ArrayList<>();
            for (final String lol : lore) {
                slore.add(lol.replace("&", "§"));
            }
            meta.setLore(slore);
        }
        this.item.setItemMeta(meta);
        this.drop = drop;
        this.move = move;
        this.command = command;
        this.cooldown = cooldown;
        this.cooldowns = new HashMap<>();
    }

    public boolean isDrop() {
        return this.drop;
    }

    public boolean isMove() {
        return this.move;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public int getSlot() {
        return this.slot;
    }

    public String getCommand() {
        return this.command;
    }

    public int getCooldown() {
        return this.cooldown;
    }
}
