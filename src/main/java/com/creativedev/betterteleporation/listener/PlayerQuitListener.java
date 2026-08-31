package com.creativedev.betterteleporation.listener;

import com.creativedev.betterteleporation.auto.AutoAcceptManager;
import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.cooldown.CooldownManager;
import com.creativedev.betterteleporation.request.TeleportRequestManager;
import com.creativedev.betterteleporation.teleport.TeleportCancelReason;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerQuitListener implements Listener {

    private final TeleportRequestManager requestManager;
    private final TeleportManager teleportManager;
    private final CooldownManager cooldownManager;
    private final CombatManager combatManager;
    private final AutoAcceptManager autoAcceptManager;

    public PlayerQuitListener(TeleportRequestManager requestManager, TeleportManager teleportManager,
                              CooldownManager cooldownManager, CombatManager combatManager,
                              AutoAcceptManager autoAcceptManager) {
        this.requestManager = requestManager;
        this.teleportManager = teleportManager;
        this.cooldownManager = cooldownManager;
        this.combatManager = combatManager;
        this.autoAcceptManager = autoAcceptManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        requestManager.removeRequestsForPlayer(uuid);
        teleportManager.cancelTask(uuid, TeleportCancelReason.DISCONNECT);
        cooldownManager.removeCooldowns(uuid);
        combatManager.remove(uuid);
        autoAcceptManager.remove(uuid);
    }
}
