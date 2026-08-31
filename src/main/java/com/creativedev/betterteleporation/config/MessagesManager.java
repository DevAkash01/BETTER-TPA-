package com.creativedev.betterteleporation.config;

import com.creativedev.betterteleporation.BetterTeleporation;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class MessagesManager {

    private final BetterTeleporation plugin;
    private final File file;
    private final Map<String, String> messageCache = new HashMap<>();
    private String prefix = "<gray>[<aqua>BetterTeleport</aqua>]</gray> ";

    public MessagesManager(BetterTeleporation plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messageCache.clear();

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            try {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                this.prefix = defaultConfig.getString("prefix", "<gray>[<aqua>BetterTeleport</aqua>]</gray> ");
                for (String key : defaultConfig.getKeys(true)) {
                    if (!defaultConfig.isConfigurationSection(key) && !key.equals("prefix")) {
                        messageCache.put(key, defaultConfig.getString(key, ""));
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (content.contains("\t")) {
                content = content.replace("\t", "  ");
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            }
            config.loadFromString(content);
        } catch (Throwable ignored) {
            try {
                config.load(file);
            } catch (Throwable ignoredFallback) {
            }
        }

        this.prefix = config.getString("prefix", this.prefix);

        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key) && !key.equals("prefix")) {
                messageCache.put(key, config.getString(key, ""));
            }
        }
    }

    public String getRawMessage(String key) {
        return messageCache.getOrDefault(key, "<red>Missing message: " + key + "</red>");
    }

    public String getPrefix() {
        return prefix;
    }
}
