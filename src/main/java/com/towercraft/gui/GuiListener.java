package com.towercraft.gui;

import com.towercraft.TowerGuiSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiListener implements Listener {
    private Gui gui;
    private String name;

    public GuiListener(final Gui gui) {
        this.gui = gui;
        this.name = gui.getDisplayName();
        TowerGuiSystem.registerListener(this);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(this.name)) {
            return;
        }
        final Player player = (Player) e.getWhoClicked();
        e.setCancelled(true);
        final GuiItem item = this.gui.getItem(e.getRawSlot());
        if (item == null) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                final String[] arr$ = item.getCommand().split(";");
                for (String cmd : arr$) {
                    if (cmd.startsWith("server:")) {
                        TowerGuiSystem.connect(player, cmd.replace("server:", ""));
                        player.closeInventory();
                    } else if (cmd.startsWith("lore")) {
                        return;
                    } else if (cmd.startsWith("close")) {
                        player.closeInventory();
                    } else {
                        Bukkit.dispatchCommand(player, cmd);
                        player.closeInventory();
                    }
                }
            }
        }.runTaskLater(TowerGuiSystem.instance, 1L);
    }
}
