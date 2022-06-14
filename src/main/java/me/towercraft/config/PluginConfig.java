package me.towercraft.config;

import me.towercraft.plugin.ioc.annotations.Bean;
import me.towercraft.plugin.ioc.annotations.Configuration;
import me.towercraft.utils.TGSLogger;

@Configuration
public class PluginConfig {

    @Bean
    public TGSLogger getTGSLogger() {
        return new TGSLogger();
    }

}
