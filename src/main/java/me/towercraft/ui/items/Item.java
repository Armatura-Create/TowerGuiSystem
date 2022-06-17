package me.towercraft.ui.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Item {
    private final ItemStack item;
    private final int slot;
    private final String command;
    private final boolean drop;
    private final boolean move;
    private final int cooldown;
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
        String[] ids = id.split(":");

        if (ids.length == 1)
            this.item = new ItemStack(Material.getMaterial(ids[0]), amount);
        else
            this.item = new ItemStack(Material.getMaterial(ids[0]), amount, Short.parseShort(ids[1]));

        this.slot = slot;

        final ItemMeta meta = this.item.getItemMeta();
        if (displayName != null && meta != null) {
            meta.setDisplayName(displayName.replace("&", "§"));
            if (lore != null)
                meta.setLore(lore.stream().map(l -> l.replace("&", "§")).collect(Collectors.toList()));
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
