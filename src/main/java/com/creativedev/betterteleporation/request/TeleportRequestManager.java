package com.creativedev.betterteleporation.request;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.api.event.TeleportRequestAcceptEvent;
import com.creativedev.betterteleporation.api.event.TeleportRequestCancelEvent;
import com.creativedev.betterteleporation.api.event.TeleportRequestCreateEvent;
import com.creativedev.betterteleporation.api.event.TeleportRequestDenyEvent;
import com.creativedev.betterteleporation.api.event.TeleportRequestExpireEvent;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.creativedev.betterteleporation.util.Permissions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TeleportRequestManager {

    private final BetterTeleporation plugin;
    private final ConfigManager configManager;
    private final Map<UUID, LinkedHashMap<UUID, TeleportRequest>> incomingRequests = new HashMap<>();
    private final Map<UUID, Map<UUID, TeleportRequest>> outgoingRequests = new HashMap<>();
    private final Object lock = new Object();
    private BukkitTask cleanupTask;

    public TeleportRequestManager(BetterTeleporation plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void start() {
        stop();
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredRequests, 20L, 20L);
    }

    public void stop() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        synchronized (lock) {
            incomingRequests.clear();
            outgoingRequests.clear();
        }
    }

    public RequestResult createRequest(Player sender, Player target, TeleportRequestType type) {
        UUID senderUuid = sender.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        boolean bypass = sender.isPermissionSet(Permissions.BYPASS_EXPIRATION) && sender.hasPermission(Permissions.BYPASS_EXPIRATION);
        long expirationMillis = bypass
                ? 86400000L * 365L
                : Math.max(10L, configManager.getExpirationSeconds()) * 1000L;

        TeleportRequest request = new TeleportRequest(
                senderUuid,
                sender.getName(),
                targetUuid,
                target.getName(),
                type,
                expirationMillis
        );

        synchronized (lock) {
            Map<UUID, TeleportRequest> outgoing = outgoingRequests.get(senderUuid);
            if (outgoing != null) {
                if (outgoing.containsKey(targetUuid)) {
                    TeleportRequest oldRequest = outgoing.get(targetUuid);
                    removeInternal(oldRequest);
                }

                if (outgoing.size() >= configManager.getMaxOutgoing()) {
                    return RequestResult.MAX_OUTGOING_REACHED;
                }
            }

            Map<UUID, TeleportRequest> incoming = incomingRequests.get(targetUuid);
            if (incoming != null && incoming.size() >= configManager.getMaxIncoming()) {
                return RequestResult.MAX_INCOMING_REACHED;
            }

            TeleportRequestCreateEvent event = new TeleportRequestCreateEvent(request);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return RequestResult.CANCELLED_BY_EVENT;
            }

            incomingRequests.computeIfAbsent(targetUuid, k -> new LinkedHashMap<>()).put(senderUuid, request);
            outgoingRequests.computeIfAbsent(senderUuid, k -> new HashMap<>()).put(targetUuid, request);

            if (configManager.isDebug()) {
                plugin.getLogger().info("[BetterTeleport] Request created: " + sender.getName() + " -> " + target.getName() + " (" + type + ")");
            }

            return RequestResult.SUCCESS;
        }
    }

    public TeleportRequest getIncomingRequest(UUID targetUuid, UUID senderUuid) {
        synchronized (lock) {
            Map<UUID, TeleportRequest> incoming = incomingRequests.get(targetUuid);
            if (incoming == null) {
                return null;
            }
            TeleportRequest request = incoming.get(senderUuid);
            if (request != null && request.isExpired()) {
                removeInternal(request);
                return null;
            }
            return request;
        }
    }

    public TeleportRequest getLatestIncomingRequest(UUID targetUuid) {
        synchronized (lock) {
            LinkedHashMap<UUID, TeleportRequest> incoming = incomingRequests.get(targetUuid);
            if (incoming == null || incoming.isEmpty()) {
                return null;
            }

            List<TeleportRequest> list = new ArrayList<>(incoming.values());
            for (int i = list.size() - 1; i >= 0; i--) {
                TeleportRequest request = list.get(i);
                if (request.isExpired()) {
                    removeInternal(request);
                } else {
                    return request;
                }
            }
            return null;
        }
    }

    public List<TeleportRequest> getIncomingRequests(UUID targetUuid) {
        synchronized (lock) {
            LinkedHashMap<UUID, TeleportRequest> incoming = incomingRequests.get(targetUuid);
            if (incoming == null || incoming.isEmpty()) {
                return Collections.emptyList();
            }

            List<TeleportRequest> valid = new ArrayList<>();
            for (TeleportRequest request : incoming.values()) {
                if (!request.isExpired()) {
                    valid.add(request);
                }
            }
            return valid;
        }
    }

    public TeleportRequest getOutgoingRequest(UUID senderUuid, UUID targetUuid) {
        synchronized (lock) {
            Map<UUID, TeleportRequest> outgoing = outgoingRequests.get(senderUuid);
            if (outgoing == null) {
                return null;
            }
            TeleportRequest request = outgoing.get(targetUuid);
            if (request != null && request.isExpired()) {
                removeInternal(request);
                return null;
            }
            return request;
        }
    }

    public List<TeleportRequest> getOutgoingRequests(UUID senderUuid) {
        synchronized (lock) {
            Map<UUID, TeleportRequest> outgoing = outgoingRequests.get(senderUuid);
            if (outgoing == null || outgoing.isEmpty()) {
                return Collections.emptyList();
            }

            List<TeleportRequest> valid = new ArrayList<>();
            for (TeleportRequest request : outgoing.values()) {
                if (!request.isExpired()) {
                    valid.add(request);
                }
            }
            return valid;
        }
    }

    public boolean acceptRequest(TeleportRequest request) {
        if (request == null) {
            return false;
        }

        synchronized (lock) {
            if (request.isExpired()) {
                removeInternal(request);
                return false;
            }

            removeInternal(request);

            TeleportRequestAcceptEvent event = new TeleportRequestAcceptEvent(request);
            Bukkit.getPluginManager().callEvent(event);

            if (configManager.isDebug()) {
                plugin.getLogger().info("[BetterTeleport] Request accepted: " + request.getSenderName() + " -> " + request.getTargetName());
            }

            return true;
        }
    }

    public void denyRequest(TeleportRequest request) {
        if (request == null) {
            return;
        }

        synchronized (lock) {
            removeInternal(request);

            TeleportRequestDenyEvent event = new TeleportRequestDenyEvent(request);
            Bukkit.getPluginManager().callEvent(event);

            if (configManager.isDebug()) {
                plugin.getLogger().info("[BetterTeleport] Request denied: " + request.getSenderName() + " -> " + request.getTargetName());
            }
        }
    }

    public void removeRequest(TeleportRequest request) {
        if (request == null) {
            return;
        }
        synchronized (lock) {
            removeInternal(request);
        }
    }

    public void removeRequestsForPlayer(UUID uuid) {
        synchronized (lock) {
            LinkedHashMap<UUID, TeleportRequest> incoming = incomingRequests.remove(uuid);
            if (incoming != null) {
                for (TeleportRequest req : incoming.values()) {
                    Map<UUID, TeleportRequest> out = outgoingRequests.get(req.getSenderUuid());
                    if (out != null) {
                        out.remove(uuid);
                    }
                }
            }

            Map<UUID, TeleportRequest> outgoing = outgoingRequests.remove(uuid);
            if (outgoing != null) {
                for (TeleportRequest req : outgoing.values()) {
                    LinkedHashMap<UUID, TeleportRequest> in = incomingRequests.get(req.getTargetUuid());
                    if (in != null) {
                        in.remove(uuid);
                    }
                }
            }
        }
    }

    public void cancelRequest(TeleportRequest request) {
        if (request == null) {
            return;
        }

        synchronized (lock) {
            removeInternal(request);

            TeleportRequestCancelEvent event = new TeleportRequestCancelEvent(request);
            Bukkit.getPluginManager().callEvent(event);

            if (configManager.isDebug()) {
                plugin.getLogger().info("[BetterTeleport] Request cancelled: " + request.getSenderName() + " -> " + request.getTargetName());
            }
        }
    }

    private void cleanupExpiredRequests() {
        List<TeleportRequest> expired = new ArrayList<>();

        synchronized (lock) {
            for (Map<UUID, TeleportRequest> map : outgoingRequests.values()) {
                for (TeleportRequest req : map.values()) {
                    if (req.isExpired()) {
                        expired.add(req);
                    }
                }
            }

            for (TeleportRequest req : expired) {
                removeInternal(req);

                TeleportRequestExpireEvent event = new TeleportRequestExpireEvent(req);
                Bukkit.getPluginManager().callEvent(event);

                Player sender = Bukkit.getPlayer(req.getSenderUuid());
                if (sender != null && sender.isOnline()) {
                    MessageUtil.sendMessage(sender, "request.expired-sender", Placeholder.parsed("player", req.getTargetName()));
                }

                Player target = Bukkit.getPlayer(req.getTargetUuid());
                if (target != null && target.isOnline()) {
                    MessageUtil.sendMessage(target, "request.expired-target", Placeholder.parsed("player", req.getSenderName()));
                }

                if (configManager.isDebug()) {
                    plugin.getLogger().info("[BetterTeleport] Request expired: " + req.getSenderName() + " -> " + req.getTargetName());
                }
            }
        }
    }

    private void removeInternal(TeleportRequest request) {
        if (request == null) {
            return;
        }
        UUID s = request.getSenderUuid();
        UUID t = request.getTargetUuid();

        Map<UUID, TeleportRequest> out = outgoingRequests.get(s);
        if (out != null) {
            out.remove(t);
            if (out.isEmpty()) {
                outgoingRequests.remove(s);
            }
        }

        LinkedHashMap<UUID, TeleportRequest> in = incomingRequests.get(t);
        if (in != null) {
            in.remove(s);
            if (in.isEmpty()) {
                incomingRequests.remove(t);
            }
        }
    }
}
