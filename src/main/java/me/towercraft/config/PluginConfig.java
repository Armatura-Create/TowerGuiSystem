package me.towercraft.config;

import unsave.plugin.context.annotations.Bean;
import unsave.plugin.context.annotations.Configuration;
import me.towercraft.utils.TGSLogger;

@Configuration
public class PluginConfig {
    @Bean
    public TGSLogger getTGSLogger() {
        return new TGSLogger();
    }

}
