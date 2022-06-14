package me.towercraft.utils;

public class TGSLogger {
    public void log(String message) {
        java.util.logging.Logger.getLogger("Minecraft").info("[TowerGuiSystem] " + message);
    }
}
