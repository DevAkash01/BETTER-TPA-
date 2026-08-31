package com.creativedev.betterteleporation.dialog;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DialogLoader {

    private final BetterTeleporation plugin;
    private final Map<String, String> templates = new ConcurrentHashMap<>();

    public DialogLoader(BetterTeleporation plugin) {
        this.plugin = plugin;
    }

    public void loadAll(ConfigManager configManager) {
        ensureDefaultDialogs();

        loadDialog("tpa-confirm", configManager.getTpaConfirmPath());
        loadDialog("tpa-received", configManager.getTpaReceivedPath());
        loadDialog("tpahere-confirm", configManager.getTpaHereConfirmPath());
        loadDialog("tpahere-received", configManager.getTpaHereReceivedPath());
    }

    public String getTemplate(String key) {
        return templates.get(key);
    }

    private void ensureDefaultDialogs() {
        File dialogsDir = new File(plugin.getDataFolder(), "dialogs");
        if (!dialogsDir.exists()) {
            dialogsDir.mkdirs();
        }

        saveDefaultDialogIfMissing("dialogs/tpa_confirm.json");
        saveDefaultDialogIfMissing("dialogs/tpa_received.json");
        saveDefaultDialogIfMissing("dialogs/tpahere_confirm.json");
        saveDefaultDialogIfMissing("dialogs/tpahere_received.json");
    }

    private void saveDefaultDialogIfMissing(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                plugin.saveResource(resourcePath, false);
            } catch (Throwable ignored) {
            }
        }
    }

    private void loadDialog(String key, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }

        File file = new File(plugin.getDataFolder(), relativePath);
        if (!file.exists()) {
            return;
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JsonElement parsed = JsonParser.parseString(content);
            if (parsed.isJsonObject()) {
                templates.put(key, content);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[BetterTeleport] Failed to load dialog template: " + relativePath);
        } catch (Throwable t) {
            plugin.getLogger().warning("[BetterTeleport] Invalid JSON in dialog template: " + relativePath + " (" + t.getMessage() + ")");
        }
    }
}
