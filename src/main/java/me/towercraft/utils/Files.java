package me.towercraft.utils;

import com.google.common.io.ByteStreams;
import me.towercraft.TGS;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Files {
    private final TGS plugin;
    private Configuration config;

    public Files(final TGS plugin) {
        this.plugin = plugin;
    }

    public void createMessages() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }
        File file = new File(this.plugin.getDataFolder(), "Messages.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (final InputStream resourceAsStream = this.plugin.getResource("Messages.yml")) {
                    final FileOutputStream fileOutputStream = new FileOutputStream(file);
                    try {
                        ByteStreams.copy(resourceAsStream, fileOutputStream);
                        this.config = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "Messages.yml"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return;
            } catch (IOException ex2) {
                throw new RuntimeException("Unable to create config file", ex2);
            }
        }
        YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "Messages.yml"));
    }

    public Configuration getMSG() {
        return config;
    }
}
