package me.towercraft.service.connect;

import me.towercraft.TGS;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.Service;
import me.towercraft.service.FileMessages;
import me.towercraft.service.server.TypeStatusServer;
import me.towercraft.utils.TGSLogger;
import me.towercraft.service.server.ServerModel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ConnectionService {

    @Autowire
    private TGS plugin;

    @Autowire
    private FileMessages fileMessages;
    @Autowire
    private TGSLogger tgsLogger;

    public void connect(Player player, String pieceTypeServer, TypeConnect typeConnect) {

        List<ServerModel> servers = new ArrayList<>();

        servers.sort(Comparator.comparing(ServerModel::getNowPlayer));

        for (final ServerModel s : servers)
            if (s.getName().contains(pieceTypeServer)
                    && s.getStatus() == TypeStatusServer.ONLINE)
                servers.add(s);

        if (servers.size() < 1)
            player.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                    fileMessages.getMSG().getString("GUI.main.wrongArgumentConnect",
                            "String not found (GUI.main.wrongArgumentConnect)")));

        switch (typeConnect) {
            case RANDOM:
                Collections.shuffle(servers);
                break;

            case MAX:
                Collections.reverse(servers);
                break;
        }

        if (servers.size() > 0) {

            if (player.getServer().getName().equalsIgnoreCase(servers.get(0).getName())) {
                player.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&',
                        fileMessages.getMSG().getString("GUI.main.areYouHere", "String not found (GUI.main.areYouHere)")) + "§a" + pieceTypeServer);
                return;
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(servers.get(0).getName());
            } catch (IOException e) {
                tgsLogger.log(e.getMessage());
            }

            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        }
    }

}
