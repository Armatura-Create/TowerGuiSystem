package com.towercraft.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import com.towercraft.TowerGuiSystem;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.List;

import static com.gmail.filoghost.holographicdisplays.api.HologramsAPI.*;
import static com.towercraft.TowerGuiSystem.getOnline;

public class HologramsDisplay {

    TowerGuiSystem plugin;

    public HologramsDisplay(TowerGuiSystem plugin) {
        this.plugin = plugin;
    }

    public Hologram createHologramPlugin(Location location) {
        return createHologram(plugin, location);
    }

    public Collection<Hologram> getHologramsPlugin() {
        return getHolograms(plugin);
    }

    public Collection<String> getRegisteredPlaceholdersPlugin() {
        return getRegisteredPlaceholders(plugin);
    }

    public boolean isHologramEntityPlugin(Entity entity) {
        return isHologramEntity(entity);
    }

    public boolean registerPlaceholderPlugin(String textPlaceholder, double refreshRate, PlaceholderReplacer replacer) {
        return registerPlaceholder(plugin, textPlaceholder, refreshRate, replacer);
    }

    public boolean unregisterPlaceholderPlugin(String textPlaceholder) {
        return unregisterPlaceholder(plugin, textPlaceholder);
    }

    public void unregisterPlaceholdersPlugin() {
        unregisterPlaceholders(plugin);
    }

    public void registerPlaceholderPluginAll(List<String> placeHolders) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    for (String placeholder : placeHolders) {
                        registerPlaceholderPlugin("{" + placeholder + "}", 1, () -> getOnline(placeholder) == -1 ? "§cOffline" : "" + getOnline(placeholder));
                        TowerGuiSystem.log("Register Placeholder - " + placeholder);
                    }

                    return;
                } catch (Exception ignore) { }
            }
        }).start();
    }
}
