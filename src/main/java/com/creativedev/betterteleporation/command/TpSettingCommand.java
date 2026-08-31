package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.settings.PlayerSettingsManager;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.creativedev.betterteleporation.util.Permissions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpSettingCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final PlayerSettingsManager settingsManager;

    public TpSettingCommand(ConfigManager configManager, PlayerSettingsManager settingsManager) {
        this.configManager = configManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission(Permissions.TP_SETTING)) {
            MessageUtil.sendMessage(player, "error.no-permission");
            return true;
        }

        if (args.length == 0) {
            boolean tpa = settingsManager.isTpaAllowed(player.getUniqueId());
            boolean tpahere = settingsManager.isTpaHereAllowed(player.getUniqueId());
            String tpaStatus = tpa ? "<green>ON</green>" : "<red>OFF</red>";
            String tpaHereStatus = tpahere ? "<green>ON</green>" : "<red>OFF</red>";
            MessageUtil.sendMessage(player, "settings.status",
                    Placeholder.parsed("tpa_status", tpaStatus),
                    Placeholder.parsed("tpahere_status", tpaHereStatus)
            );
            return true;
        }

        String targetSetting = args[0].toLowerCase();

        if (targetSetting.equals("tpa")) {
            if (args.length >= 2) {
                String sub = args[1].toLowerCase();
                if (sub.equals("on") || sub.equals("enable") || sub.equals("true")) {
                    settingsManager.setTpaAllowed(player.getUniqueId(), true);
                    MessageUtil.sendMessage(player, "settings.tpa-enabled");
                    configManager.getSoundManager().playSettingEnabled(player);
                    return true;
                } else if (sub.equals("off") || sub.equals("disable") || sub.equals("false")) {
                    settingsManager.setTpaAllowed(player.getUniqueId(), false);
                    MessageUtil.sendMessage(player, "settings.tpa-disabled");
                    configManager.getSoundManager().playSettingDisabled(player);
                    return true;
                } else if (sub.equals("toggle")) {
                    boolean newState = !settingsManager.isTpaAllowed(player.getUniqueId());
                    settingsManager.setTpaAllowed(player.getUniqueId(), newState);
                    MessageUtil.sendMessage(player, newState ? "settings.tpa-enabled" : "settings.tpa-disabled");
                    if (newState) {
                        configManager.getSoundManager().playSettingEnabled(player);
                    } else {
                        configManager.getSoundManager().playSettingDisabled(player);
                    }
                    return true;
                }
            }
            boolean newState = !settingsManager.isTpaAllowed(player.getUniqueId());
            settingsManager.setTpaAllowed(player.getUniqueId(), newState);
            MessageUtil.sendMessage(player, newState ? "settings.tpa-enabled" : "settings.tpa-disabled");
            if (newState) {
                configManager.getSoundManager().playSettingEnabled(player);
            } else {
                configManager.getSoundManager().playSettingDisabled(player);
            }
            return true;
        }

        if (targetSetting.equals("tpahere")) {
            if (args.length >= 2) {
                String sub = args[1].toLowerCase();
                if (sub.equals("on") || sub.equals("enable") || sub.equals("true")) {
                    settingsManager.setTpaHereAllowed(player.getUniqueId(), true);
                    MessageUtil.sendMessage(player, "settings.tpahere-enabled");
                    configManager.getSoundManager().playSettingEnabled(player);
                    return true;
                } else if (sub.equals("off") || sub.equals("disable") || sub.equals("false")) {
                    settingsManager.setTpaHereAllowed(player.getUniqueId(), false);
                    MessageUtil.sendMessage(player, "settings.tpahere-disabled");
                    configManager.getSoundManager().playSettingDisabled(player);
                    return true;
                } else if (sub.equals("toggle")) {
                    boolean newState = !settingsManager.isTpaHereAllowed(player.getUniqueId());
                    settingsManager.setTpaHereAllowed(player.getUniqueId(), newState);
                    MessageUtil.sendMessage(player, newState ? "settings.tpahere-enabled" : "settings.tpahere-disabled");
                    if (newState) {
                        configManager.getSoundManager().playSettingEnabled(player);
                    } else {
                        configManager.getSoundManager().playSettingDisabled(player);
                    }
                    return true;
                }
            }
            boolean newState = !settingsManager.isTpaHereAllowed(player.getUniqueId());
            settingsManager.setTpaHereAllowed(player.getUniqueId(), newState);
            MessageUtil.sendMessage(player, newState ? "settings.tpahere-enabled" : "settings.tpahere-disabled");
            if (newState) {
                configManager.getSoundManager().playSettingEnabled(player);
            } else {
                configManager.getSoundManager().playSettingDisabled(player);
            }
            return true;
        }

        player.sendMessage(command.getUsage());
        return true;
    }
}
