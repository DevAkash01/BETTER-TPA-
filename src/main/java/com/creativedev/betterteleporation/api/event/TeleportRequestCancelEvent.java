package com.creativedev.betterteleporation.api.event;

import com.creativedev.betterteleporation.request.TeleportRequest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class TeleportRequestCancelEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final TeleportRequest request;

    public TeleportRequestCancelEvent(TeleportRequest request) {
        this.request = request;
    }

    public TeleportRequest getRequest() {
        return request;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
