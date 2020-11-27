package me.towercraft.gui;

import me.towercraft.TGS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiListener implements Listener {
    private final Gui gui;
    private final String name;

    public GuiListener(final Gui gui) {
        this.gui = gui;
        this.name = gui.getDisplayName();
        TGS.registerListener(this);
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

        if (item.getServerModel() == null || item.getServerModel().getInStatus().equals("online"))
            new BukkitRunnable() {
                public void run() {
                    final String[] arr$ = item.getCommand().split(";");
                    for (String cmd : arr$) {
                        if (cmd.startsWith("server:")) {
                            TGS.connect(player, cmd.replace("server:", "") + "_random");
                            player.closeInventory();
                        } else if (cmd.startsWith("maxLobby:")) {
                            TGS.connect(player, cmd.replace("maxLobby:", "") + "_max");
                            player.closeInventory();
                        } else if (cmd.startsWith("minLobby:")) {
                            TGS.connect(player, cmd.replace("minLobby:", "") + "_min");
                            player.closeInventory();
                        } else if (cmd.startsWith("lore")) {
                            return;
                        } else if (cmd.startsWith("close")) {
                            player.closeInventory();
                        } else if (cmd.startsWith("gui")) {
                            Bukkit.dispatchCommand(player, cmd);
                        } else {
                            Bukkit.dispatchCommand(player, cmd);
                            player.closeInventory();
                        }
                    }
                }
            }.runTaskLater(TGS.instance, 1L);
    }
}
