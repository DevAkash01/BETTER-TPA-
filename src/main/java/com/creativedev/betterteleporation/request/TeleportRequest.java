package com.creativedev.betterteleporation.request;

import java.util.Objects;
import java.util.UUID;

public final class TeleportRequest {

    private final UUID id;
    private final UUID senderUuid;
    private final String senderName;
    private final UUID targetUuid;
    private final String targetName;
    private final TeleportRequestType type;
    private final long createdAt;
    private final long expiresAt;

    public TeleportRequest(UUID senderUuid, String senderName, UUID targetUuid, String targetName, TeleportRequestType type, long expirationDurationMillis) {
        this.id = UUID.randomUUID();
        this.senderUuid = Objects.requireNonNull(senderUuid, "senderUuid cannot be null");
        this.senderName = Objects.requireNonNull(senderName, "senderName cannot be null");
        this.targetUuid = Objects.requireNonNull(targetUuid, "targetUuid cannot be null");
        this.targetName = Objects.requireNonNull(targetName, "targetName cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.createdAt = System.currentTimeMillis();
        long exp = this.createdAt + Math.max(1000L, expirationDurationMillis);
        this.expiresAt = exp < this.createdAt ? Long.MAX_VALUE : exp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public TeleportRequestType getType() {
        return type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        if (expiresAt <= 0L || expiresAt == Long.MAX_VALUE) {
            return false;
        }
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getRemainingSeconds() {
        if (expiresAt == Long.MAX_VALUE) {
            return 86400L;
        }
        long remaining = (expiresAt - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleportRequest that = (TeleportRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
