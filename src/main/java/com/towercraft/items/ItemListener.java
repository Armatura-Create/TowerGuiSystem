package com.towercraft.items;

import com.towercraft.TowerGuiSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class ItemListener implements Listener {
    private ItemManager manager;

    public ItemListener() {
        this.manager = TowerGuiSystem.instance.itemManager;
        TowerGuiSystem.registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = e.getPlayer();

        String language = "";
        try {
            language = player.getLocale().toLowerCase().split("_")[0];
        } catch (Exception ignore){
            language = TowerGuiSystem.defaultLanguage;
        }

        final Item item = this.manager.getItem(language, player.getItemInHand());
        if (item == null) {
            return;
        }
        e.setCancelled(true);
        if (item.getCooldown() > 0) {
            final String name = player.getName().toLowerCase();
            if (item.cooldowns.containsKey(name)) {
                return;
            }
            item.cooldowns.put(name, item.getCooldown());
            new BukkitRunnable() {
                public void run() {
                    item.cooldowns.remove(name);
                }
            }.runTaskLaterAsynchronously(TowerGuiSystem.instance, item.getCooldown() * 20L);
        }
        if (item.getCommand() == null) {
            return;
        }
        final String[] arr$ = item.getCommand().split(";");
        for (String cmd : arr$) {
            if (cmd.startsWith("/")) {
                player.chat(cmd);
            } else {
                Bukkit.dispatchCommand(player, cmd);
            }
        }
    }

    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
        final ItemStack itemStack = e.getItemDrop().getItemStack();

        String language = "";
        try {
            language = e.getPlayer().getLocale().toLowerCase().split("_")[0];
        } catch (Exception ignore){
            language = TowerGuiSystem.defaultLanguage;
        }

        final Item item = this.manager.getItem(language, itemStack);
        if (item == null) {
            return;
        }
        if (item.isDrop()) {
            return;
        }
        e.getItemDrop().getItemStack().setAmount(0);
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        String language = "";
        try {
            language = p.getLocale().toLowerCase().split("_")[0];
        } catch (Exception ignore){
            language = TowerGuiSystem.defaultLanguage;
        }

        final Item item = this.manager.getItem(language, e.getCurrentItem());
        if (item == null) {
            return;
        }
        if (item.isMove()) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        if (TowerGuiSystem.instance.clearOnJoin) {
            player.getInventory().clear();
        }
        new BukkitRunnable() {
            public void run() {
                if (!player.isOnline()) {
                    return;
                }

                TowerGuiSystem.log(player.getLocale().toLowerCase().split("_")[0]);

                String language = "";
                try {
                    language = player.getLocale().toLowerCase().split("_")[0];
                } catch (Exception ignore){
                    language = TowerGuiSystem.defaultLanguage;
                }

                Map<Integer, Item> listItems = ItemListener.this.manager.items.get(language);
                if (listItems == null || listItems.size() == 0)
                    listItems = ItemListener.this.manager.items.get(TowerGuiSystem.defaultLanguage);

                for (final Integer slot : listItems.keySet()) {
                    final Item item = listItems.get(slot);
                    final ItemStack itemStack = player.getInventory().getItem(slot);
                    player.getInventory().setItem(slot, item.getItemStack());
                    if (itemStack == null || itemStack.getType() == Material.AIR || ItemListener.this.manager.getItem(player.getLocale().toLowerCase().split("_")[0], itemStack) != null) {
                        continue;
                    }
                    player.getInventory().addItem(itemStack);
                }
                player.updateInventory();
            }
        }.runTaskLaterAsynchronously(TowerGuiSystem.instance, 0L);
    }
}