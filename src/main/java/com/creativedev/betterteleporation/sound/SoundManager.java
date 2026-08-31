package com.creativedev.betterteleporation.sound;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class SoundManager {

    private SoundEffect requestSent;
    private SoundEffect requestReceived;
    private SoundEffect requestAccepted;
    private SoundEffect requestDenied;
    private SoundEffect settingEnabled;
    private SoundEffect settingDisabled;
    private SoundEffect countdownTick;
    private float countdownPitchIncrease;
    private SoundEffect countdownFinalSecond;
    private SoundEffect teleportSuccess;
    private SoundEffect teleportCancel;

    public SoundManager() {
        loadDefaults();
    }

    public void load(FileConfiguration config) {
        if (config == null || !config.isConfigurationSection("sounds")) {
            loadDefaults();
            return;
        }

        this.requestSent = parseSound(config, "sounds.request-sent", Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.2f);
        this.requestReceived = parseSound(config, "sounds.request-received", Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.4f);
        this.requestAccepted = parseSound(config, "sounds.request-accepted", Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.requestDenied = parseSound(config, "sounds.request-denied", Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        this.settingEnabled = parseSound(config, "sounds.setting-enabled", Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        this.settingDisabled = parseSound(config, "sounds.setting-disabled", Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);

        if (config.isConfigurationSection("sounds.countdown-tick")) {
            this.countdownTick = parseSound(config, "sounds.countdown-tick", Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
            this.countdownPitchIncrease = (float) config.getDouble("sounds.countdown-tick.pitch-increase-per-second", 0.15);
        } else if (config.isConfigurationSection("sounds.teleport-warmup-tick")) {
            this.countdownTick = parseSound(config, "sounds.teleport-warmup-tick", Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.3f);
            this.countdownPitchIncrease = 0.0f;
        } else {
            this.countdownTick = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
            this.countdownPitchIncrease = 0.15f;
        }

        if (config.isConfigurationSection("sounds.countdown-final-second")) {
            this.countdownFinalSecond = parseSound(config, "sounds.countdown-final-second", Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 2.0f);
        } else {
            this.countdownFinalSecond = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 2.0f);
        }

        this.teleportSuccess = parseSound(config, "sounds.teleport-success", Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        this.teleportCancel = parseSound(config, "sounds.teleport-cancel", Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    private void loadDefaults() {
        this.requestSent = new SoundEffect(true, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.2f);
        this.requestReceived = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.4f);
        this.requestAccepted = new SoundEffect(true, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.requestDenied = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        this.settingEnabled = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        this.settingDisabled = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
        this.countdownTick = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
        this.countdownPitchIncrease = 0.15f;
        this.countdownFinalSecond = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 2.0f);
        this.teleportSuccess = new SoundEffect(true, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        this.teleportCancel = new SoundEffect(true, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    private SoundEffect parseSound(FileConfiguration config, String path, Sound defaultSound, float defaultVol, float defaultPitch) {
        boolean enabled = config.getBoolean(path + ".enabled", true);
        String soundName = config.getString(path + ".sound", defaultSound.name());
        float volume = (float) config.getDouble(path + ".volume", defaultVol);
        float pitch = (float) config.getDouble(path + ".pitch", defaultPitch);
        return new SoundEffect(enabled, soundName, defaultSound, volume, pitch);
    }

    public void playRequestSent(Player player) {
        if (requestSent != null) {
            requestSent.play(player);
        }
    }

    public void playRequestReceived(Player player) {
        if (requestReceived != null) {
            requestReceived.play(player);
        }
    }

    public void playRequestAccepted(Player player) {
        if (requestAccepted != null) {
            requestAccepted.play(player);
        }
    }

    public void playRequestDenied(Player player) {
        if (requestDenied != null) {
            requestDenied.play(player);
        }
    }

    public void playSettingEnabled(Player player) {
        if (settingEnabled != null) {
            settingEnabled.play(player);
        }
    }

    public void playSettingDisabled(Player player) {
        if (settingDisabled != null) {
            settingDisabled.play(player);
        }
    }

    public void playCountdownTick(Player player, int secondsRemaining, int totalSeconds) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (secondsRemaining == 1 && countdownFinalSecond != null) {
            countdownFinalSecond.play(player);
            return;
        }

        if (countdownTick == null) {
            return;
        }

        int elapsed = totalSeconds - secondsRemaining;
        float pitch = countdownTick.getPitch() + (elapsed * countdownPitchIncrease);
        countdownTick.play(player, pitch);
    }

    public void playTeleportSuccess(Player player) {
        if (teleportSuccess != null) {
            teleportSuccess.play(player);
        }
    }

    public void playTeleportCancel(Player player) {
        if (teleportCancel != null) {
            teleportCancel.play(player);
        }
    }
}
