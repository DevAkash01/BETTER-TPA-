package com.creativedev.betterteleporation.dialog;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestType;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.dialog.Dialog;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class DialogManager {

    private final BetterTeleporation plugin;
    private final ConfigManager configManager;
    private final DialogLoader dialogLoader;

    public DialogManager(BetterTeleporation plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dialogLoader = new DialogLoader(plugin);
        load();
    }

    public void load() {
        dialogLoader.loadAll(configManager);
    }

    public void openTpaConfirmation(Player sender, Player target) {
        if (sender == null || !sender.isOnline() || target == null || !target.isOnline()) {
            return;
        }

        String template = dialogLoader.getTemplate("tpa-confirm");
        if (template == null) {
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", target.getName());
        placeholders.put("target_name", target.getName());
        placeholders.put("sender", sender.getName());
        placeholders.put("sender_name", sender.getName());
        placeholders.put("player", target.getName());
        placeholders.put("player_name", target.getName());

        String resolved = DialogPlaceholderResolver.resolve(template, placeholders);
        try {
            JsonObject json = JsonParser.parseString(resolved).getAsJsonObject();
            Dialog dialog = DialogParser.parse(json, "/tpa confirm " + target.getName(), "/tpa cancel");
            if (dialog != null) {
                sender.showDialog(dialog);
            }
        } catch (Throwable ignored) {
        }
    }

    public void openTpaHereConfirmation(Player sender, Player target) {
        if (sender == null || !sender.isOnline() || target == null || !target.isOnline()) {
            return;
        }

        String template = dialogLoader.getTemplate("tpahere-confirm");
        if (template == null) {
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", target.getName());
        placeholders.put("target_name", target.getName());
        placeholders.put("sender", sender.getName());
        placeholders.put("sender_name", sender.getName());
        placeholders.put("player", target.getName());
        placeholders.put("player_name", target.getName());

        String resolved = DialogPlaceholderResolver.resolve(template, placeholders);
        try {
            JsonObject json = JsonParser.parseString(resolved).getAsJsonObject();
            Dialog dialog = DialogParser.parse(json, "/tpahere confirm " + target.getName(), "/tpahere cancel");
            if (dialog != null) {
                sender.showDialog(dialog);
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean openTpaReceivedConfirmation(Player target, TeleportRequest request) {
        if (target == null || !target.isOnline() || request == null) {
            return false;
        }

        String template = dialogLoader.getTemplate("tpa-received");
        if (template == null) {
            return false;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("requester", request.getSenderName());
        placeholders.put("requester_name", request.getSenderName());
        placeholders.put("sender", request.getSenderName());
        placeholders.put("sender_name", request.getSenderName());
        placeholders.put("target", target.getName());
        placeholders.put("target_name", target.getName());
        placeholders.put("player", request.getSenderName());
        placeholders.put("player_name", request.getSenderName());

        String resolved = DialogPlaceholderResolver.resolve(template, placeholders);
        try {
            JsonObject json = JsonParser.parseString(resolved).getAsJsonObject();
            Dialog dialog = DialogParser.parse(json, "/tpaccept direct " + request.getSenderName(), "/tpdeny " + request.getSenderName());
            if (dialog != null) {
                target.showDialog(dialog);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public boolean openTpaHereReceivedConfirmation(Player target, TeleportRequest request) {
        if (target == null || !target.isOnline() || request == null) {
            return false;
        }

        String template = dialogLoader.getTemplate("tpahere-received");
        if (template == null) {
            return false;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("requester", request.getSenderName());
        placeholders.put("requester_name", request.getSenderName());
        placeholders.put("sender", request.getSenderName());
        placeholders.put("sender_name", request.getSenderName());
        placeholders.put("target", target.getName());
        placeholders.put("target_name", target.getName());
        placeholders.put("player", request.getSenderName());
        placeholders.put("player_name", request.getSenderName());

        String resolved = DialogPlaceholderResolver.resolve(template, placeholders);
        try {
            JsonObject json = JsonParser.parseString(resolved).getAsJsonObject();
            Dialog dialog = DialogParser.parse(json, "/tpaccept direct " + request.getSenderName(), "/tpdeny " + request.getSenderName());
            if (dialog != null) {
                target.showDialog(dialog);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public void sendRequestNotification(Player target, TeleportRequest request) {
        if (target == null || !target.isOnline()) {
            return;
        }

        String senderName = request.getSenderName();
        boolean isTpa = request.getType() == TeleportRequestType.TPA;
        String msgKey = isTpa ? "request.received-tpa" : "request.received-tpahere";
        String actionBarKey = isTpa ? "request.received-tpa-actionbar" : "request.received-tpahere-actionbar";

        MessageUtil.sendMessage(target, msgKey, Placeholder.parsed("player", senderName));
        MessageUtil.sendActionBar(target, actionBarKey, Placeholder.parsed("player", senderName));

        configManager.getSoundManager().playRequestReceived(target);
    }
}
