package me.towercraft.items;

import me.towercraft.TowerGuiSystem;
import org.bukkit.Bukkit;
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

        final Item item = this.manager.getItem(player.getItemInHand());
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

        final Item item = this.manager.getItem(itemStack);
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

        final Item item = this.manager.getItem(e.getCurrentItem());
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

                Map<Integer, Item> listItems = ItemListener.this.manager.items;

                for (final Integer slot : listItems.keySet()) {
                    final Item item = listItems.get(slot);
                    final ItemStack itemStack = player.getInventory().getItem(slot);

                    if (ItemListener.this.manager.getItem(itemStack) != null)
                        continue;

                    if (itemStack == null)
                        player.getInventory().setItem(slot, item.getItemStack());

                    if (itemStack != null && TowerGuiSystem.instance.replaceItemOnJoin)
                        player.getInventory().setItem(slot, item.getItemStack());
                }
                player.updateInventory();
            }
        }.runTaskLaterAsynchronously(TowerGuiSystem.instance, 10L);
    }
}