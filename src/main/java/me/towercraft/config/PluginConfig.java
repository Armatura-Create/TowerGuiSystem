package me.towercraft.config;

import me.towercraft.TGS;
import me.towercraft.service.HologramsDisplay;
import me.towercraft.service.NameServerService;
import me.towercraft.service.PlaceHolderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.annotations.Bean;
import unsave.plugin.context.annotations.Configuration;
import me.towercraft.utils.TGSLogger;

@Configuration
public class PluginConfig {

    @Autowire
    private TGS plugin;

    @Bean
    public TGSLogger getTGSLogger() {
        return new TGSLogger();
    }

    @Bean
    public NameServerService getNameServerService() {
        return new NameServerService();
    }

    @Bean
    public PlaceHolderExpansion getPlaceHolderExpansion() {
        return new PlaceHolderExpansion();
    }

    @Bean
    public HologramsDisplay getHo() {
        if (plugin.getConfig().getBoolean("Enable.HologramsDisplay", false)) {
            Plugin holographicDisplays = Bukkit.getPluginManager().getPlugin("HolographicDisplays");

            if (holographicDisplays != null) {
                return new HologramsDisplay();
            } else
                throw new RuntimeException("Could not find HolographicDisplays!! Plugin can not work without it!");
        }

        return null;
    }

}
