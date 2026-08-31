package com.creativedev.betterteleporation.util;

import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.config.MessagesManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEX_AMPERSAND_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BRACKET_PATTERN = Pattern.compile("\\{&#?([A-Fa-f0-9]{6})\\}");
    private static final Pattern HEX_BUKKIT_PATTERN = Pattern.compile("[&§]x[&§]([A-Fa-f0-9])[&§]([A-Fa-f0-9])[&§]([A-Fa-f0-9])[&§]([A-Fa-f0-9])[&§]([A-Fa-f0-9])[&§]([A-Fa-f0-9])");

    private static ConfigManager configManager;
    private static MessagesManager messagesManager;

    private MessageUtil() {
    }

    public static void init(ConfigManager configMgr, MessagesManager messagesMgr) {
        configManager = configMgr;
        messagesManager = messagesMgr;
    }

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String processed = preprocess(text);
        return MINI_MESSAGE.deserialize(processed);
    }

    public static Component parse(String text, TagResolver... tagResolvers) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String processed = preprocess(text);
        return MINI_MESSAGE.deserialize(processed, tagResolvers);
    }

    public static Component parseKey(String key) {
        String raw = messagesManager != null ? messagesManager.getRawMessage(key) : key;
        return parse(raw);
    }

    public static Component parseKey(String key, TagResolver... tagResolvers) {
        String raw = messagesManager != null ? messagesManager.getRawMessage(key) : key;
        return parse(raw, tagResolvers);
    }

    public static Component parseKey(String key, Map<String, String> placeholders) {
        List<TagResolver> resolvers = new ArrayList<>();
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                resolvers.add(Placeholder.parsed(entry.getKey(), entry.getValue()));
            }
        }
        return parseKey(key, resolvers.toArray(new TagResolver[0]));
    }

    public static void sendMessage(Audience audience, String key) {
        if (audience != null) {
            audience.sendMessage(parseKey(key));
        }
    }

    public static void sendMessage(Audience audience, String key, TagResolver... tagResolvers) {
        if (audience != null) {
            audience.sendMessage(parseKey(key, tagResolvers));
        }
    }

    public static void sendMessage(Audience audience, String key, Map<String, String> placeholders) {
        if (audience != null) {
            audience.sendMessage(parseKey(key, placeholders));
        }
    }

    public static void sendActionBar(Player player, String key, TagResolver... tagResolvers) {
        if (player != null) {
            player.sendActionBar(parseKey(key, tagResolvers));
        }
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player != null && sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private static String preprocess(String text) {
        String prefix = messagesManager != null ? messagesManager.getPrefix() : "";
        String processed = text.replace("<prefix>", prefix);

        if (configManager != null) {
            processed = processed.replace("<primary>", "<color:" + configManager.getPrimaryColor() + ">")
                    .replace("</primary>", "</color>")
                    .replace("<secondary>", "<color:" + configManager.getSecondaryColor() + ">")
                    .replace("</secondary>", "</color>")
                    .replace("<accent>", "<color:" + configManager.getAccentColor() + ">")
                    .replace("</accent>", "</color>")
                    .replace("<error>", "<color:" + configManager.getErrorColor() + ">")
                    .replace("</error>", "</color>")
                    .replace("<warning>", "<color:" + configManager.getWarningColor() + ">")
                    .replace("</warning>", "</color>");

            if (configManager.isHexColorsEnabled()) {
                processed = convertHexAndLegacy(processed);
            }
        }

        return processed;
    }

    private static String convertHexAndLegacy(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher bukkitMatcher = HEX_BUKKIT_PATTERN.matcher(input);
        if (bukkitMatcher.find()) {
            input = bukkitMatcher.replaceAll("<#$1$2$3$4$5$6>");
        }

        Matcher ampersandMatcher = HEX_AMPERSAND_PATTERN.matcher(input);
        if (ampersandMatcher.find()) {
            input = ampersandMatcher.replaceAll("<#$1>");
        }

        Matcher bracketMatcher = HEX_BRACKET_PATTERN.matcher(input);
        if (bracketMatcher.find()) {
            input = bracketMatcher.replaceAll("<#$1>");
        }

        return convertLegacyCodes(input);
    }

    private static String convertLegacyCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c == '&' || c == '§') && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                String replacement = switch (code) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> null;
                };
                if (replacement != null) {
                    sb.append(replacement);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
