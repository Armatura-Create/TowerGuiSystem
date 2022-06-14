package me.towercraft;

import me.towercraft.plugin.ioc.context.PluginApplicationContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TGS extends JavaPlugin {

    public static boolean isUpdate = false;

    private PluginApplicationContext context;

    @Override
    public void onEnable() {

        context = new PluginApplicationContext(TGS.class);
        context.registerBean(this.getClass().getSimpleName(), this);

        this.saveDefaultConfig();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        context.invokeDestroy();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
    }

    public String getPrefix() {
        return "§6TGS §8» §7";
    }
}
