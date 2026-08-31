package com.creativedev.betterteleporation.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundEffect {

    private final boolean enabled;
    private final String rawSoundName;
    private final Sound bukkitSound;
    private final float volume;
    private final float pitch;

    public SoundEffect(boolean enabled, String rawSoundName, Sound fallbackSound, float volume, float pitch) {
        this.enabled = enabled;
        this.rawSoundName = rawSoundName != null && !rawSoundName.isEmpty() ? rawSoundName.trim() : (fallbackSound != null ? fallbackSound.name() : "");
        this.bukkitSound = resolveBukkitSound(this.rawSoundName, fallbackSound);
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundEffect(boolean enabled, Sound sound, float volume, float pitch) {
        this(enabled, sound != null ? sound.name() : "", sound, volume, pitch);
    }

    public void play(Player player) {
        play(player, this.pitch);
    }

    public void play(Player player, float customPitch) {
        if (!enabled || player == null || !player.isOnline()) {
            return;
        }

        try {
            if (bukkitSound != null) {
                player.playSound(player.getLocation(), bukkitSound, volume, customPitch);
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (rawSoundName != null && !rawSoundName.isEmpty()) {
                String clean = rawSoundName.toLowerCase().replace('_', '.');
                if (!clean.contains(":")) {
                    clean = "minecraft:" + clean;
                }
                player.playSound(net.kyori.adventure.sound.Sound.sound(Key.key(clean), Source.PLAYER, volume, customPitch));
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (rawSoundName != null && !rawSoundName.isEmpty()) {
                player.playSound(player.getLocation(), rawSoundName, volume, customPitch);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Sound resolveBukkitSound(String name, Sound fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }

        String formatted = name.toUpperCase().replace('.', '_').replace('-', '_');
        if (formatted.startsWith("MINECRAFT:")) {
            formatted = formatted.substring(10);
        }

        try {
            return Sound.valueOf(formatted);
        } catch (Throwable ignored) {
        }

        for (Sound s : Sound.values()) {
            if (s.name().equalsIgnoreCase(formatted) || s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }

        return fallback;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRawSoundName() {
        return rawSoundName;
    }

    public Sound getBukkitSound() {
        return bukkitSound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
