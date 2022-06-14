package me.towercraft.ui.gui;

import me.towercraft.TGS;
import me.towercraft.service.server.ServerModel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiItem {

    private int slot;
    private String command;
    private ItemStack item;
    public Iterator<List<String>> iterator;
    private List<List<String>> animation;
    private List<String> lore = new ArrayList<>();
    private ServerModel serverModel;
    private String server;

    public GuiItem(Gui gui,
                   String id,
                   int amount,
                   String name,
                   List<String> lore,
                   int slot,
                   String command,
                   List<List<String>> animation,
                   ServerModel serverModel,
                   String server) {

        List<String> lore_result = new ArrayList<>();
        lore_result.addAll(lore);

        List<String> x_lore = new ArrayList<>();
        for (final String lol : lore_result)
            x_lore.add(lol.replace("&", "§"));

        lore_result = x_lore;

        this.slot = slot;
        this.command = command;
        this.serverModel = serverModel;
        this.server = server;
        if (amount == 0) {
            amount = 1;
        }
        try {
            final String[] xid = id.split(":");
            ItemStack itemStack;
            if (xid.length == 2) {
                final int subid = Integer.parseInt(xid[1]);
                itemStack = new ItemStack(Material.getMaterial(xid[0]), amount, (short) subid);
            } else {
                itemStack = new ItemStack(Material.getMaterial(xid[0]), amount);
            }
            final ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(name);
            if (lore_result.size() > 0) {
                this.lore = lore_result;
            }
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
