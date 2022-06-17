package me.towercraft.commandListeners;

import me.towercraft.TGS;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.Component;
import unsave.plugin.context.annotations.PostConstruct;
import me.towercraft.utils.FileMessages;
import me.towercraft.service.GuiService;
import me.towercraft.service.ItemService;
import me.towercraft.service.connect.ConnectionService;
import me.towercraft.service.connect.TypeConnect;
import me.towercraft.ui.gui.Gui;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Component
public class PluginCommandListener implements CommandExecutor {

    @Autowire
    private TGS plugin;

    @Autowire
    private FileMessages fileMessages;

    @Autowire
    private GuiService guiService;

    @Autowire
    private ItemService itemService;

    @Autowire
    private ConnectionService connectionService;

    @PostConstruct
    public void init() {
        plugin.getCommand("gui").setExecutor(this);
        plugin.getCommand("connect").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String commandLabel, String[] args) {
        switch (commandLabel.split("_")[0]) {
            case "gui":
                if (args.length == 0) {
                    for (String temp : fileMessages.getMSG().getStringList("GUI.help"))
                        sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', temp));
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("gui.reload")) {
                        sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                                fileMessages.getMSG().getString("GUI.reload.noPermissions", "String not found (GUI.reload.noPermissions)")));
                        return true;
                    }

                    guiService.load();
                    itemService.load();

                    sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                            fileMessages.getMSG().getString("GUI.reload.loadComplete", "String not found (GUI.reload.loadComplete)")));
                    return true;
                }

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("list")) {
                        sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                                fileMessages.getMSG().getString("GUI.main.listGui", "String not found (GUI.main.listGui)")));

                        for (final String name : guiService.getGuis().keySet()) {
                            sender.sendMessage("§f- §c" + name);
                        }
                        return true;
                    }
                }

                if (!(sender instanceof Player)) {
                    return false;
                }

                if (args.length == 2) {
                    final Player player = (Player) sender;
                    if (args[0].equalsIgnoreCase("open")) {
                        final Gui gui = guiService.getGuis().get(args[1]);
                        if (gui == null) {
                            commandNotFound(sender);
                            return true;
                        }
                        gui.open(player);
                        return true;
                    }
                }

                for (String temp : fileMessages.getMSG().getStringList("GUI.help"))
                    sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', temp));

                return true;

            case "connect":
                if (sender instanceof Player)
                    if (args.length == 2) {
                        TypeConnect typeConnect = TypeConnect.valueOf(args[1]);
                        connectionService.connect((Player) sender, args[0], typeConnect);
                    } else
                        commandNotFound(sender);
                return true;

            default:
                commandNotFound(sender);
                return true;
        }
    }

    private void commandNotFound(CommandSender sender) {
        sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                fileMessages.getMSG().getString("GUI.main.wrongArgumentConnect", "Sting not found (GUI.main.wrongArgumentConnect)")));
    }
}
