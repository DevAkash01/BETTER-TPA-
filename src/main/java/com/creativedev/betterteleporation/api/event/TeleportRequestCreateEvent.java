package com.creativedev.betterteleporation.api.event;

import com.creativedev.betterteleporation.request.TeleportRequest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class TeleportRequestCreateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final TeleportRequest request;
    private boolean cancelled;

    public TeleportRequestCreateEvent(TeleportRequest request) {
        this.request = request;
    }

    public TeleportRequest getRequest() {
        return request;
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
