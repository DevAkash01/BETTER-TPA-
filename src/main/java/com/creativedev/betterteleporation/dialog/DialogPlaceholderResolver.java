package com.creativedev.betterteleporation.dialog;

import java.util.Map;

public final class DialogPlaceholderResolver {

    private DialogPlaceholderResolver() {
    }

    public static String resolve(String jsonTemplate, Map<String, String> placeholders) {
        if (jsonTemplate == null || jsonTemplate.isEmpty() || placeholders == null || placeholders.isEmpty()) {
            return jsonTemplate;
        }

        String result = jsonTemplate;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : "";

            result = result.replace("{" + key + "}", value)
                    .replace("%" + key + "%", value);
        }

        return result;
    }
}
