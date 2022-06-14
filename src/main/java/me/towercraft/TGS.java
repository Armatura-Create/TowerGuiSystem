package me.towercraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import unsave.plugin.context.context.PluginApplicationContext;

public final class TGS extends JavaPlugin {
    private PluginApplicationContext context;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        context = new PluginApplicationContext(this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        if (context != null)
            context.invokeDestroy();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
    }

    public String getPrefix() {
        return "§6TGS §8» §7";
    }
}
