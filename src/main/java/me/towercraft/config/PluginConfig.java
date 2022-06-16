package me.towercraft.config;

import me.towercraft.service.NameServerService;
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

}
