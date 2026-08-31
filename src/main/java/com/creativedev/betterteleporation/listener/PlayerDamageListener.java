package com.creativedev.betterteleporation.listener;

import com.creativedev.betterteleporation.combat.CombatManager;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.teleport.TeleportCancelReason;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class PlayerDamageListener implements Listener {

    private final ConfigManager configManager;
    private final TeleportManager teleportManager;
    private final CombatManager combatManager;

    public PlayerDamageListener(ConfigManager configManager, TeleportManager teleportManager, CombatManager combatManager) {
        this.configManager = configManager;
        this.teleportManager = teleportManager;
        this.combatManager = combatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (configManager.isCancelOnDamage()) {
            teleportManager.cancelTask(player.getUniqueId(), TeleportCancelReason.DAMAGE);
        }

        if (configManager.isCombatEnabled()) {
            combatManager.tagCombat(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!configManager.isCombatEnabled()) {
            return;
        }

        if (event.getDamager() instanceof Player player) {
            combatManager.tagCombat(player.getUniqueId());
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                combatManager.tagCombat(player.getUniqueId());
            }
        }
    }
}
