package com.creativedev.betterteleporation.command;

import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TeleportTabCompleter implements TabCompleter {

    private final TeleportRequestManager requestManager;

    public TeleportTabCompleter(TeleportRequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("tpa")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> completions = new ArrayList<>();
                if (sender.hasPermission(Permissions.ADMIN_RELOAD)) {
                    if ("reload".startsWith(input)) {
                        completions.add("reload");
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
                return completions;
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("tpahere")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> completions = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
                return completions;
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("tpauto")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> sub = new ArrayList<>();
                for (String option : List.of("on", "off", "toggle")) {
                    if (option.startsWith(input)) {
                        sub.add(option);
                    }
                }
                return sub;
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("tpsetting")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> sub = new ArrayList<>();
                for (String option : List.of("tpa", "tpahere")) {
                    if (option.startsWith(input)) {
                        sub.add(option);
                    }
                }
                return sub;
            } else if (args.length == 2) {
                String input = args[1].toLowerCase();
                List<String> sub = new ArrayList<>();
                for (String option : List.of("on", "off", "toggle")) {
                    if (option.startsWith(input)) {
                        sub.add(option);
                    }
                }
                return sub;
            }
            return Collections.emptyList();
        }

        if (args.length != 1) {
            return Collections.emptyList();
        }

        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        String input = args[0].toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmdName.equals("tpaccept") || cmdName.equals("tpdeny")) {
            List<TeleportRequest> incoming = requestManager.getIncomingRequests(player.getUniqueId());
            for (TeleportRequest req : incoming) {
                if (req.getSenderName().toLowerCase().startsWith(input)) {
                    completions.add(req.getSenderName());
                }
            }
        }

        return completions;
    }
}
