package com.creativedev.betterteleporation;

import com.creativedev.betterteleporation.auto.AutoAcceptManager;
import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.command.TeleportTabCompleter;
import com.creativedev.betterteleporation.command.TpAutoCommand;
import com.creativedev.betterteleporation.command.TpSettingCommand;
import com.creativedev.betterteleporation.command.TpaAcceptCommand;
import com.creativedev.betterteleporation.command.TpaCommand;
import com.creativedev.betterteleporation.command.TpaDenyCommand;
import com.creativedev.betterteleporation.command.TpaHereCommand;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.config.MessagesManager;
import com.creativedev.betterteleporation.cooldown.CooldownManager;
import com.creativedev.betterteleporation.dialog.DialogManager;
import com.creativedev.betterteleporation.listener.PlayerDamageListener;
import com.creativedev.betterteleporation.listener.PlayerDeathListener;
import com.creativedev.betterteleporation.listener.PlayerMoveListener;
import com.creativedev.betterteleporation.listener.PlayerQuitListener;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.settings.PlayerSettingsManager;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import com.creativedev.betterteleporation.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterTeleporation extends JavaPlugin {

    private static BetterTeleporation instance;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private TeleportRequestManager requestManager;
    private TeleportManager teleportManager;
    private CooldownManager cooldownManager;
    private CombatManager combatManager;
    private DialogManager dialogManager;
    private AutoAcceptManager autoAcceptManager;
    private PlayerSettingsManager settingsManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.requestManager = new TeleportRequestManager(this, configManager);
        this.teleportManager = new TeleportManager(this, configManager);
        this.cooldownManager = new CooldownManager();
        this.combatManager = new CombatManager(configManager);
        this.dialogManager = new DialogManager(this, configManager);
        this.autoAcceptManager = new AutoAcceptManager();
        this.settingsManager = new PlayerSettingsManager(this);

        MessageUtil.init(configManager, messagesManager);

        registerListeners();
        registerCommands();

        printBanner(true);
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }

        if (settingsManager != null) {
            settingsManager.save();
        }

        printBanner(false);

        instance = null;
    }

    private void printBanner(boolean enabled) {
        String status = enabled ? "<green><b>✓ Plugin Enabled</b></green>" : "<red><b>✗ Plugin Disabled</b></red>";
        String version = getPluginMeta().getVersion();

        String[] lines = new String[] {
                "<gradient:#00d2ff:#3a7bd5><b>╔══════════════════════════════════════════════════════╗</b></gradient>",
                "<gradient:#00d2ff:#3a7bd5><b>║                                                      ║</b></gradient>",
                "<gradient:#00d2ff:#3a7bd5><b>║              T E C H N O C R A T                     ║</b></gradient>",
                "<gradient:#00d2ff:#3a7bd5><b>║                    S T U D I O                       ║</b></gradient>",
                "<gradient:#00d2ff:#3a7bd5><b>║                                                      ║</b></gradient>",
                "<b>║              " + status + "                       ║</b>",
                "<b>║              <gray>✓ Version: " + version + "</gray>                        ║</b>",
                "<gradient:#00d2ff:#3a7bd5><b>║                                                      ║</b></gradient>",
                "<gradient:#00d2ff:#3a7bd5><b>╚══════════════════════════════════════════════════════╝</b></gradient>"
        };

        for (String line : lines) {
            Component parsed = MessageUtil.parse(line);
            getComponentLogger().info(parsed);
        }
    }

    public void reloadPlugin() {
        configManager.load();
        messagesManager.load();
        dialogManager.load();
        settingsManager.load();
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerQuitListener(requestManager, teleportManager, cooldownManager, combatManager, autoAcceptManager), this);
        pm.registerEvents(new PlayerDamageListener(configManager, teleportManager, combatManager), this);
        pm.registerEvents(new PlayerDeathListener(configManager, teleportManager), this);
        pm.registerEvents(new PlayerMoveListener(configManager, teleportManager), this);
    }

    private void registerCommands() {
        TeleportTabCompleter tabCompleter = new TeleportTabCompleter(requestManager);

        PluginCommand tpa = getCommand("tpa");
        if (tpa != null) {
            tpa.setExecutor(new TpaCommand(this, configManager, requestManager, teleportManager, cooldownManager, combatManager, dialogManager, autoAcceptManager, settingsManager));
            tpa.setTabCompleter(tabCompleter);
        }

        PluginCommand tpahere = getCommand("tpahere");
        if (tpahere != null) {
            tpahere.setExecutor(new TpaHereCommand(configManager, requestManager, teleportManager, cooldownManager, combatManager, dialogManager, autoAcceptManager, settingsManager));
            tpahere.setTabCompleter(tabCompleter);
        }

        PluginCommand tpauto = getCommand("tpauto");
        if (tpauto != null) {
            tpauto.setExecutor(new TpAutoCommand(autoAcceptManager));
            tpauto.setTabCompleter(tabCompleter);
        }

        PluginCommand tpsetting = getCommand("tpsetting");
        if (tpsetting != null) {
            tpsetting.setExecutor(new TpSettingCommand(configManager, settingsManager));
            tpsetting.setTabCompleter(tabCompleter);
        }

        PluginCommand tpaccept = getCommand("tpaccept");
        if (tpaccept != null) {
            tpaccept.setExecutor(new TpaAcceptCommand(configManager, requestManager, teleportManager, combatManager, dialogManager));
            tpaccept.setTabCompleter(tabCompleter);
        }

        PluginCommand tpdeny = getCommand("tpdeny");
        if (tpdeny != null) {
            tpdeny.setExecutor(new TpaDenyCommand(configManager, requestManager));
            tpdeny.setTabCompleter(tabCompleter);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public TeleportRequestManager getRequestManager() {
        return requestManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public AutoAcceptManager getAutoAcceptManager() {
        return autoAcceptManager;
    }

    public PlayerSettingsManager getSettingsManager() {
        return settingsManager;
    }

    public static BetterTeleporation getInstance() {
        return instance;
    }
}
