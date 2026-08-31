package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.auto.AutoAcceptManager;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.creativedev.betterteleporation.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpAutoCommand implements CommandExecutor {

    private final AutoAcceptManager autoAcceptManager;

    public TpAutoCommand(AutoAcceptManager autoAcceptManager) {
        this.autoAcceptManager = autoAcceptManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission(Permissions.TP_AUTO)) {
            MessageUtil.sendMessage(player, "error.no-permission");
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            if (sub.equals("on") || sub.equals("enable") || sub.equals("true")) {
                autoAcceptManager.setAutoAccept(player.getUniqueId(), true);
                MessageUtil.sendMessage(player, "auto.enabled");
                return true;
            } else if (sub.equals("off") || sub.equals("disable") || sub.equals("false")) {
                autoAcceptManager.setAutoAccept(player.getUniqueId(), false);
                MessageUtil.sendMessage(player, "auto.disabled");
                return true;
            } else if (sub.equals("toggle")) {
                boolean state = autoAcceptManager.toggle(player.getUniqueId());
                MessageUtil.sendMessage(player, state ? "auto.enabled" : "auto.disabled");
                return true;
            }
        }

        boolean state = autoAcceptManager.toggle(player.getUniqueId());
        MessageUtil.sendMessage(player, state ? "auto.enabled" : "auto.disabled");
        return true;
    }
}
