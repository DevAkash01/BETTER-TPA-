package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
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

public final class TpaDenyCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final TeleportRequestManager requestManager;

    public TpaDenyCommand(ConfigManager configManager, TeleportRequestManager requestManager) {
        this.configManager = configManager;
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission(Permissions.TP_DENY)) {
            MessageUtil.sendMessage(player, "error.no-permission");
            return true;
        }

        TeleportRequest request;

        if (args.length > 0) {
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

        requestManager.denyRequest(request);
        MessageUtil.sendMessage(player, "request.denied-target", Placeholder.parsed("player", request.getSenderName()));
        configManager.getSoundManager().playRequestDenied(player);

        Player requester = Bukkit.getPlayer(request.getSenderUuid());
        if (requester != null && requester.isOnline()) {
            MessageUtil.sendMessage(requester, "request.denied-sender", Placeholder.parsed("player", player.getName()));
            configManager.getSoundManager().playRequestDenied(requester);
        }

        return true;
    }
}
