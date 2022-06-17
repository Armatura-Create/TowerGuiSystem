package me.towercraft.ui.gui;

import me.towercraft.service.server.ServerModel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiItem {

    private final int slot;
    private final String command;
    private ItemStack item;
    public Iterator<List<String>> iterator;
    private final List<List<String>> animation;
    private final List<String> lore;
    private final ServerModel serverModel;
    private final String server;

    public GuiItem(String id,
                   int amount,
                   String name,
                   List<String> lore,
                   int slot,
                   String command,
                   List<List<String>> animation,
                   ServerModel serverModel,
                   String server) {

        this.lore = lore.stream().map(l -> l.replace("&", "§")).collect(Collectors.toList());
        this.slot = slot;
        this.command = command;
        this.serverModel = serverModel;
        this.server = server;
        if (amount == 0) {
            amount = 1;
        }
        try {
            String[] xid = id.split(":");
            ItemStack itemStack;
            if (xid.length == 2)
                itemStack = new ItemStack(Material.getMaterial(xid[0]), amount, Short.parseShort(xid[1]));
            else
                itemStack = new ItemStack(Material.getMaterial(xid[0]), amount);

            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null)
                meta.setDisplayName(name);

            itemStack.setItemMeta(meta);
            this.item = itemStack;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.animation = animation;
        if (animation != null) {
            this.iterator = animation.iterator();
        }
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

    public List<List<String>> getAnimation() {
        return animation;
    }

    public List<String> getLore() {
        return lore;
    }

    public ServerModel getServerModel() {
        return serverModel;
    }

    public String getServer() {
        return server;
    }

    public ItemStack getItem() {
        return item;
    }
}
