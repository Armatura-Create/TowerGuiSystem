package me.towercraft.config;

import me.towercraft.service.NameServerService;
import me.towercraft.service.PlaceHolderExpansion;
import unsave.plugin.context.annotations.Bean;
import unsave.plugin.context.annotations.Configuration;
import me.towercraft.utils.TGSLogger;

@Configuration
public class PluginConfig {
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

}
