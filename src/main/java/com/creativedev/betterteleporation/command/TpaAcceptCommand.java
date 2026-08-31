package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.dialog.DialogManager;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.request.TeleportRequestType;
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

import java.util.List;

public final class TpaAcceptCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final TeleportRequestManager requestManager;
    private final TeleportManager teleportManager;
    private final CombatManager combatManager;
    private final DialogManager dialogManager;

    public TpaAcceptCommand(ConfigManager configManager, TeleportRequestManager requestManager,
                            TeleportManager teleportManager, CombatManager combatManager,
                            DialogManager dialogManager) {
        this.configManager = configManager;
        this.requestManager = requestManager;
        this.teleportManager = teleportManager;
        this.combatManager = combatManager;
        this.dialogManager = dialogManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission(Permissions.TP_ACCEPT)) {
            MessageUtil.sendMessage(player, "error.no-permission");
            return true;
        }

        if (configManager.isCombatBlockTeleports() && combatManager.isInCombat(player)) {
            long remaining = combatManager.getRemainingCombatSeconds(player.getUniqueId());
            MessageUtil.sendMessage(player, "error.combat", Placeholder.parsed("seconds", String.valueOf(remaining)));
            return true;
        }

        boolean isDirect = args.length > 0 && args[0].equalsIgnoreCase("direct");
        TeleportRequest request;

        if (isDirect && args.length >= 2) {
            String targetName = args[1];
            Player requester = Bukkit.getPlayer(targetName);
            if (requester != null) {
                request = requestManager.getIncomingRequest(player.getUniqueId(), requester.getUniqueId());
            } else {
                List<TeleportRequest> incoming = requestManager.getIncomingRequests(player.getUniqueId());
                request = incoming.stream()
                        .filter(r -> r.getSenderName().equalsIgnoreCase(targetName))
                        .findFirst()
                        .orElse(null);
            }

            if (request == null) {
                MessageUtil.sendMessage(player, "error.no-request-from-player", Placeholder.parsed("player", targetName));
                return true;
            }
        } else if (args.length > 0 && !isDirect) {
            String targetName = args[0];
            Player requester = Bukkit.getPlayer(targetName);
            if (requester != null) {
                request = requestManager.getIncomingRequest(player.getUniqueId(), requester.getUniqueId());
            } else {
                List<TeleportRequest> incoming = requestManager.getIncomingRequests(player.getUniqueId());
                request = incoming.stream()
                        .filter(r -> r.getSenderName().equalsIgnoreCase(targetName))
                        .findFirst()
                        .orElse(null);
            }

            if (request == null) {
                MessageUtil.sendMessage(player, "error.no-request-from-player", Placeholder.parsed("player", targetName));
                return true;
            }
        } else {
            request = requestManager.getLatestIncomingRequest(player.getUniqueId());
            if (request == null) {
                MessageUtil.sendMessage(player, "error.no-pending-requests");
                return true;
            }
        }

        Player requester = Bukkit.getPlayer(request.getSenderUuid());
        if (requester == null || !requester.isOnline()) {
            requestManager.removeRequest(request);
            MessageUtil.sendMessage(player, "error.player-not-found", Placeholder.parsed("player", request.getSenderName()));
            return true;
        }

        if (isDirect || !configManager.isDialogEnabled()) {
            if (requestManager.acceptRequest(request)) {
                MessageUtil.sendMessage(player, "request.accepted-target", Placeholder.parsed("player", requester.getName()));
                MessageUtil.sendMessage(requester, "request.accepted-sender", Placeholder.parsed("player", player.getName()));
                configManager.getSoundManager().playRequestAccepted(player);
                configManager.getSoundManager().playRequestAccepted(requester);
                teleportManager.startTeleport(request);
            } else {
                MessageUtil.sendMessage(player, "error.request-invalid");
            }
            return true;
        }

        boolean opened = request.getType() == TeleportRequestType.TPA
                ? dialogManager.openTpaReceivedConfirmation(player, request)
                : dialogManager.openTpaHereReceivedConfirmation(player, request);

        if (!opened) {
            if (requestManager.acceptRequest(request)) {
                MessageUtil.sendMessage(player, "request.accepted-target", Placeholder.parsed("player", requester.getName()));
                MessageUtil.sendMessage(requester, "request.accepted-sender", Placeholder.parsed("player", player.getName()));
                configManager.getSoundManager().playRequestAccepted(player);
                configManager.getSoundManager().playRequestAccepted(requester);
                teleportManager.startTeleport(request);
            } else {
                MessageUtil.sendMessage(player, "error.request-invalid");
            }
        }

        return true;
    }
}
