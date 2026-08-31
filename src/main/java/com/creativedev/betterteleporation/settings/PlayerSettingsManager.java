package com.creativedev.betterteleporation.settings;

import com.creativedev.betterteleporation.BetterTeleporation;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSettingsManager {

    private final BetterTeleporation plugin;
    private final Map<UUID, Boolean> tpaDisabled = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> tpaHereDisabled = new ConcurrentHashMap<>();

    public PlayerSettingsManager(BetterTeleporation plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        tpaDisabled.clear();
        tpaHereDisabled.clear();

        File file = new File(plugin.getDataFolder(), "settings.yml");
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean tpa = config.getBoolean(key + ".tpa", true);
                boolean tpaHere = config.getBoolean(key + ".tpahere", true);
                if (!tpa) {
                    tpaDisabled.put(uuid, true);
                }
                if (!tpaHere) {
                    tpaHereDisabled.put(uuid, true);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "settings.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (UUID uuid : tpaDisabled.keySet()) {
            config.set(uuid.toString() + ".tpa", false);
        }
        for (UUID uuid : tpaHereDisabled.keySet()) {
            config.set(uuid.toString() + ".tpahere", false);
        }

        try {
            config.save(file);
        } catch (Throwable ignored) {
        }
    }

    public boolean isTpaAllowed(UUID uuid) {
        if (uuid == null) {
            return true;
        }
        return !tpaDisabled.getOrDefault(uuid, false);
    }

    public void setTpaAllowed(UUID uuid, boolean allowed) {
        if (uuid == null) {
            return;
        }
        if (allowed) {
            tpaDisabled.remove(uuid);
        } else {
            tpaDisabled.put(uuid, true);
        }
        save();
    }

    public boolean isTpaHereAllowed(UUID uuid) {
        if (uuid == null) {
            return true;
        }
        return !tpaHereDisabled.getOrDefault(uuid, false);
    }

    public void setTpaHereAllowed(UUID uuid, boolean allowed) {
        if (uuid == null) {
            return;
        }
        if (allowed) {
            tpaHereDisabled.remove(uuid);
        } else {
            tpaHereDisabled.put(uuid, true);
        }
        save();
    }
}
