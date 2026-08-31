package com.creativedev.betterteleporation.api;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.config.MessagesManager;
import com.creativedev.betterteleporation.cooldown.CooldownManager;
import com.creativedev.betterteleporation.request.RequestResult;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.request.TeleportRequestType;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class BetterTeleporationAPI {

    private static BetterTeleporationAPI instance;
    private final BetterTeleporation plugin;

    public BetterTeleporationAPI(BetterTeleporation plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static BetterTeleporationAPI getInstance() {
        return instance;
    }

    public TeleportRequestManager getRequestManager() {
        return plugin.getRequestManager();
    }

    public TeleportManager getTeleportManager() {
        return plugin.getTeleportManager();
    }

    public CooldownManager getCooldownManager() {
        return plugin.getCooldownManager();
    }

    public CombatManager getCombatManager() {
        return plugin.getCombatManager();
    }

    public ConfigManager getConfigManager() {
        return plugin.getConfigManager();
    }

    public MessagesManager getMessagesManager() {
        return plugin.getMessagesManager();
    }

    public RequestResult sendRequest(Player sender, Player target, TeleportRequestType type) {
        return plugin.getRequestManager().createRequest(sender, target, type);
    }

    public boolean acceptRequest(TeleportRequest request) {
        if (plugin.getRequestManager().acceptRequest(request)) {
            return plugin.getTeleportManager().startTeleport(request);
        }
        return false;
    }

    public void denyRequest(TeleportRequest request) {
        plugin.getRequestManager().denyRequest(request);
    }

    public void cancelRequest(TeleportRequest request) {
        plugin.getRequestManager().removeRequest(request);
    }

    public TeleportRequest getIncomingRequest(UUID targetUuid, UUID senderUuid) {
        return plugin.getRequestManager().getIncomingRequest(targetUuid, senderUuid);
    }

    public TeleportRequest getLatestIncomingRequest(UUID targetUuid) {
        return plugin.getRequestManager().getLatestIncomingRequest(targetUuid);
    }

    public List<TeleportRequest> getIncomingRequests(UUID targetUuid) {
        return plugin.getRequestManager().getIncomingRequests(targetUuid);
    }

    public List<TeleportRequest> getOutgoingRequests(UUID senderUuid) {
        return plugin.getRequestManager().getOutgoingRequests(senderUuid);
    }
}
