package com.creativedev.betterteleporation.api.event;

import com.creativedev.betterteleporation.request.TeleportRequest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class TeleportRequestTeleportEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final TeleportRequest request;
    private final Player player;
    private Location destination;
    private boolean cancelled;

    public TeleportRequestTeleportEvent(TeleportRequest request, Player player, Location destination) {
        this.request = request;
        this.player = player;
        this.destination = destination;
    }

    public TeleportRequest getRequest() {
        return request;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
