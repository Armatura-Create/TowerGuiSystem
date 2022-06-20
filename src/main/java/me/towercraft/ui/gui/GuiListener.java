package me.towercraft.ui.gui;

import me.towercraft.TGS;
import me.towercraft.service.connect.ConnectionService;
import me.towercraft.service.connect.TypeConnect;
import me.towercraft.service.server.TypeStatusServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiListener implements Listener {
    private final Gui gui;
    private final String name;
    private final TGS plugin;
    private final ConnectionService connectionService;

    public GuiListener(Gui gui, TGS plugin, ConnectionService connectionService) {
        this.gui = gui;
        this.name = gui.getDisplayName();
        this.plugin = plugin;
        this.connectionService = connectionService;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(this.name)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        e.setCancelled(true);

        GuiItem item = this.gui.getItem(e.getRawSlot());
        if (item == null) {
            return;
        }

        if (item.getServerModel().getStatus() == TypeStatusServer.ONLINE)
            new BukkitRunnable() {
                public void run() {
                    String[] args = item.getCommand().split(";");
                    for (String cmd : args) {
                        if (cmd.startsWith("server:")) {
                            connectionService.connect(player, cmd.replace("server:", ""), TypeConnect.RANDOM);
                            player.closeInventory();
                        } else if (cmd.startsWith("maxLobby:")) {
                            connectionService.connect(player, cmd.replace("maxLobby:", ""), TypeConnect.MAX);
                            player.closeInventory();
                        } else if (cmd.startsWith("minLobby:")) {
                            connectionService.connect(player, cmd.replace("minLobby:", ""), TypeConnect.MIN);
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
            }.runTaskLater(plugin, 1L);
    }
}
