package com.creativedev.betterteleporation.config;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.sound.SoundManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConfigManager {

    private final BetterTeleporation plugin;
    private final SoundManager soundManager = new SoundManager();

    private long expirationSeconds;
    private int maxOutgoing;
    private int maxIncoming;
    private boolean preventDuplicates;
    private int teleportDelaySeconds;
    private boolean cancelOnMove;
    private double moveThreshold;
    private boolean cancelOnDamage;
    private boolean cancelOnDeath;
    private boolean playSound;
    private int cooldownTpa;
    private int cooldownTpaHere;
    private boolean dialogEnabled;
    private boolean fallbackToChat;
    private String tpaConfirmPath;
    private String tpaReceivedPath;
    private String tpaHereConfirmPath;
    private String tpaHereReceivedPath;
    private boolean worldRestrictionsEnabled;
    private Set<String> disabledWorlds;
    private boolean combatEnabled;
    private int combatDurationSeconds;
    private boolean combatBlockRequests;
    private boolean combatBlockTeleports;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String errorColor;
    private String warningColor;
    private boolean debug;

    public ConfigManager(BetterTeleporation plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
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

        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            try {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                config.setDefaults(defaultConfig);
            } catch (Throwable ignored) {
            }
        }

        this.expirationSeconds = config.getLong("request.expiration-seconds", 60L);
        this.maxOutgoing = config.getInt("request.max-outgoing", 3);
        this.maxIncoming = config.getInt("request.max-incoming", 10);
        this.preventDuplicates = config.getBoolean("request.prevent-duplicates", true);

        this.teleportDelaySeconds = config.getInt("teleport.delay-seconds", config.getInt("teleport.delay", config.getInt("teleport.warmup", 3)));
        this.cancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        this.moveThreshold = config.getDouble("teleport.move-threshold", 0.5);
        this.cancelOnDamage = config.getBoolean("teleport.cancel-on-damage", true);
        this.cancelOnDeath = config.getBoolean("teleport.cancel-on-death", true);
        this.playSound = config.getBoolean("teleport.play-sound", true);

        this.cooldownTpa = config.getInt("cooldown.tpa", 5);
        this.cooldownTpaHere = config.getInt("cooldown.tpahere", 5);

        this.dialogEnabled = config.getBoolean("dialog.enabled", true);
        this.fallbackToChat = config.getBoolean("dialog.fallback-to-chat", true);

        this.tpaConfirmPath = config.getString("dialogs.tpa-confirm", "dialogs/tpa_confirm.json");
        this.tpaReceivedPath = config.getString("dialogs.tpa-received", "dialogs/tpa_received.json");
        this.tpaHereConfirmPath = config.getString("dialogs.tpahere-confirm", "dialogs/tpahere_confirm.json");
        this.tpaHereReceivedPath = config.getString("dialogs.tpahere-received", "dialogs/tpahere_received.json");

        this.worldRestrictionsEnabled = config.getBoolean("world-restrictions.enabled", false);
        List<String> worldsList = config.getStringList("world-restrictions.disabled-worlds");
        this.disabledWorlds = new HashSet<>(worldsList);

        this.combatEnabled = config.getBoolean("combat.enabled", false);
        this.combatDurationSeconds = config.getInt("combat.combat-duration-seconds", 15);
        this.combatBlockRequests = config.getBoolean("combat.block-requests", true);
        this.combatBlockTeleports = config.getBoolean("combat.block-teleports", true);

        this.primaryColor = config.getString("colors.primary", "#00d2ff");
        this.secondaryColor = config.getString("colors.secondary", "#3a7bd5");
        this.accentColor = config.getString("colors.accent", "#00ff87");
        this.errorColor = config.getString("colors.error", "#ff4b4b");
        this.warningColor = config.getString("colors.warning", "#ffaa00");

        this.soundManager.load(config);

        this.debug = config.getBoolean("debug", false);
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public int getMaxOutgoing() {
        return maxOutgoing;
    }

    public int getMaxIncoming() {
        return maxIncoming;
    }

    public boolean isPreventDuplicates() {
        return preventDuplicates;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }

    public boolean isCancelOnMove() {
        return cancelOnMove;
    }

    public double getMoveThreshold() {
        return moveThreshold;
    }

    public boolean isCancelOnDamage() {
        return cancelOnDamage;
    }

    public boolean isCancelOnDeath() {
        return cancelOnDeath;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public int getCooldownTpa() {
        return cooldownTpa;
    }

    public int getCooldownTpaHere() {
        return cooldownTpaHere;
    }

    public boolean isDialogEnabled() {
        return dialogEnabled;
    }

    public boolean isFallbackToChat() {
        return fallbackToChat;
    }

    public String getTpaConfirmPath() {
        return tpaConfirmPath;
    }

    public String getTpaReceivedPath() {
        return tpaReceivedPath;
    }

    public String getTpaHereConfirmPath() {
        return tpaHereConfirmPath;
    }

    public String getTpaHereReceivedPath() {
        return tpaHereReceivedPath;
    }

    public boolean isWorldRestrictionsEnabled() {
        return worldRestrictionsEnabled;
    }

    public Set<String> getDisabledWorlds() {
        return Collections.unmodifiableSet(disabledWorlds);
    }

    public boolean isWorldDisabled(String worldName) {
        if (!worldRestrictionsEnabled) {
            return false;
        }
        return disabledWorlds.contains(worldName);
    }

    public boolean isCombatEnabled() {
        return combatEnabled;
    }

    public int getCombatDurationSeconds() {
        return combatDurationSeconds;
    }

    public boolean isCombatBlockRequests() {
        return combatBlockRequests;
    }

    public boolean isCombatBlockTeleports() {
        return combatBlockTeleports;
    }

    public boolean isHexColorsEnabled() {
        return true;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getErrorColor() {
        return errorColor;
    }

    public String getWarningColor() {
        return warningColor;
    }

    public boolean isDebug() {
        return debug;
    }
}
