package me.towercraft.utils;

import me.towercraft.TGS;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceHolderExpansion extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "towerguisystem";
    }

    @Override
    public String getRequiredPlugin() {
        return "TowerGuiSystem";
    }

    @Override
    public String getAuthor() {
        return TGS.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return TGS.plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer p, String params) {

        if (params == null)
            return "";
        if (params.equals("servername")) {
            if (TGS.nameServer != null)
                return TGS.nameServer.split("-")[0];
            else
                return "NameServer";
        }
        if (params.equals("servernamewithnumber")) {
            if (TGS.nameServer != null)
                return TGS.nameServer;
            else
                return "NameServer";
        }
        if (params.equals("onlineamount")) {
            return TGS.getAllOnline() + "";
        }
        if (params.contains("serveramount")) {
            return TGS.lobbys.stream().filter(serverModel -> serverModel.getName().split("-")[0].equalsIgnoreCase(params.split("_")[1])).count() + "";
        }
        return super.onRequest(p, params);
    }
}
