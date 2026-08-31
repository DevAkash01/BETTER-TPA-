package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.auto.AutoAcceptManager;
import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.cooldown.CooldownManager;
import com.creativedev.betterteleporation.dialog.DialogManager;
import com.creativedev.betterteleporation.request.RequestResult;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.request.TeleportRequestType;
import com.creativedev.betterteleporation.settings.PlayerSettingsManager;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.creativedev.betterteleporation.util.Permissions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpaHereCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final TeleportRequestManager requestManager;
    private final TeleportManager teleportManager;
    private final CooldownManager cooldownManager;
    private final CombatManager combatManager;
    private final DialogManager dialogManager;
    private final AutoAcceptManager autoAcceptManager;
    private final PlayerSettingsManager settingsManager;

    public TpaHereCommand(ConfigManager configManager, TeleportRequestManager requestManager,
                          TeleportManager teleportManager, CooldownManager cooldownManager,
                          CombatManager combatManager, DialogManager dialogManager,
                          AutoAcceptManager autoAcceptManager, PlayerSettingsManager settingsManager) {
        this.configManager = configManager;
        this.requestManager = requestManager;
        this.teleportManager = teleportManager;
        this.cooldownManager = cooldownManager;
        this.combatManager = combatManager;
        this.dialogManager = dialogManager;
        this.autoAcceptManager = autoAcceptManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission(Permissions.TPA_HERE)) {
            MessageUtil.sendMessage(player, "error.no-permission");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(command.getUsage());
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            return true;
        }

        boolean isConfirmed = args[0].equalsIgnoreCase("confirm") && args.length >= 2;
        String targetName = isConfirmed ? args[1] : args[0];

        if (cooldownManager.isOnCooldown(player, "tpahere")) {
            long remaining = cooldownManager.getRemainingSeconds(player.getUniqueId(), "tpahere");
            MessageUtil.sendMessage(player, "error.cooldown", Placeholder.parsed("seconds", String.valueOf(remaining)));
            return true;
        }

        if (configManager.isCombatBlockRequests() && combatManager.isInCombat(player)) {
            long remaining = combatManager.getRemainingCombatSeconds(player.getUniqueId());
            MessageUtil.sendMessage(player, "error.combat", Placeholder.parsed("seconds", String.valueOf(remaining)));
            return true;
        }

        if (configManager.isWorldDisabled(player.getWorld().getName())) {
            MessageUtil.sendMessage(player, "error.world-disabled");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "error.player-not-found", Placeholder.parsed("player", targetName));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "error.self-request");
            return true;
        }

        if (configManager.isWorldDisabled(target.getWorld().getName())) {
            MessageUtil.sendMessage(player, "error.world-disabled");
            return true;
        }

        if (!settingsManager.isTpaHereAllowed(target.getUniqueId())) {
            MessageUtil.sendMessage(player, "error.target-tpahere-disabled", Placeholder.parsed("player", target.getName()));
            return true;
        }

        if (!isConfirmed && configManager.isDialogEnabled()) {
            dialogManager.openTpaHereConfirmation(player, target);
            return true;
        }

        sendRequest(player, target);
        return true;
    }

    private void sendRequest(Player player, Player target) {
        RequestResult result = requestManager.createRequest(player, target, TeleportRequestType.TPA_HERE);
        switch (result) {
            case SUCCESS -> {
                cooldownManager.setCooldown(player.getUniqueId(), "tpahere", configManager.getCooldownTpaHere());
                TeleportRequest request = requestManager.getIncomingRequest(target.getUniqueId(), player.getUniqueId());

                if (autoAcceptManager.isAutoAccept(target.getUniqueId()) && request != null) {
                    if (requestManager.acceptRequest(request)) {
                        MessageUtil.sendMessage(player, "auto.accepted-sender", Placeholder.parsed("player", target.getName()));
                        MessageUtil.sendMessage(target, "auto.accepted-target", Placeholder.parsed("player", player.getName()));
                        configManager.getSoundManager().playRequestAccepted(player);
                        configManager.getSoundManager().playRequestAccepted(target);
                        teleportManager.startTeleport(request);
                        return;
                    }
                }

                MessageUtil.sendMessage(player, "request.sent-here", Placeholder.parsed("player", target.getName()));
                configManager.getSoundManager().playRequestSent(player);
                if (request != null) {
                    dialogManager.sendRequestNotification(target, request);
                }
            }
            case DUPLICATE -> MessageUtil.sendMessage(player, "error.duplicate-request", Placeholder.parsed("player", target.getName()));
            case MAX_OUTGOING_REACHED -> MessageUtil.sendMessage(player, "error.max-outgoing", Placeholder.parsed("max", String.valueOf(configManager.getMaxOutgoing())));
            case MAX_INCOMING_REACHED -> MessageUtil.sendMessage(player, "error.max-incoming");
            case CANCELLED_BY_EVENT -> {}
        }
    }
}
