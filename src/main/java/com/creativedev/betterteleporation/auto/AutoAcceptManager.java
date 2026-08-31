package com.creativedev.betterteleporation.auto;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AutoAcceptManager {

    private final Set<UUID> autoAcceptPlayers = ConcurrentHashMap.newKeySet();

    public boolean isAutoAccept(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return autoAcceptPlayers.contains(uuid);
    }

    public void setAutoAccept(UUID uuid, boolean enable) {
        if (uuid == null) {
            return;
        }
        if (enable) {
            autoAcceptPlayers.add(uuid);
        } else {
            autoAcceptPlayers.remove(uuid);
        }
    }

    public boolean toggle(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        if (autoAcceptPlayers.contains(uuid)) {
            autoAcceptPlayers.remove(uuid);
            return false;
        } else {
            autoAcceptPlayers.add(uuid);
            return true;
        }
    }

    public void remove(UUID uuid) {
        if (uuid != null) {
            autoAcceptPlayers.remove(uuid);
        }
    }

    public void clear() {
        autoAcceptPlayers.clear();
    }
}
