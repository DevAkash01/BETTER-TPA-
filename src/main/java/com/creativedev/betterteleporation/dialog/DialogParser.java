package com.creativedev.betterteleporation.dialog;

import com.creativedev.betterteleporation.util.MessageUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class DialogParser {

    private DialogParser() {
    }

    public static Dialog parse(JsonObject json, String confirmCommand, String cancelCommand) {
        if (json == null) {
            return null;
        }

        Component title = parseComponent(json.get("title"));
        DialogBase.Builder baseBuilder = DialogBase.builder(title);

        if (json.has("external_title")) {
            baseBuilder.externalTitle(parseComponent(json.get("external_title")));
        } else if (json.has("externalTitle")) {
            baseBuilder.externalTitle(parseComponent(json.get("externalTitle")));
        }

        if (json.has("canCloseWithEscape")) {
            baseBuilder.canCloseWithEscape(json.get("canCloseWithEscape").getAsBoolean());
        } else if (json.has("can_close_with_escape")) {
            baseBuilder.canCloseWithEscape(json.get("can_close_with_escape").getAsBoolean());
        } else {
            baseBuilder.canCloseWithEscape(true);
        }

        if (json.has("pause")) {
            baseBuilder.pause(json.get("pause").getAsBoolean());
        }

        if (json.has("after_action")) {
            String afterActionStr = json.get("after_action").getAsString().toLowerCase();
            DialogBase.DialogAfterAction afterAction = switch (afterActionStr) {
                case "none" -> DialogBase.DialogAfterAction.NONE;
                case "wait_for_response" -> DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE;
                default -> DialogBase.DialogAfterAction.CLOSE;
            };
            baseBuilder.afterAction(afterAction);
        } else if (json.has("afterAction")) {
            String afterActionStr = json.get("afterAction").getAsString().toLowerCase();
            DialogBase.DialogAfterAction afterAction = switch (afterActionStr) {
                case "none" -> DialogBase.DialogAfterAction.NONE;
                case "wait_for_response" -> DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE;
                default -> DialogBase.DialogAfterAction.CLOSE;
            };
            baseBuilder.afterAction(afterAction);
        } else {
            baseBuilder.afterAction(DialogBase.DialogAfterAction.CLOSE);
        }

        List<DialogBody> bodies = new ArrayList<>();
        if (json.has("body") && json.get("body").isJsonArray()) {
            JsonArray bodyArray = json.getAsJsonArray("body");
            for (JsonElement elem : bodyArray) {
                if (elem.isJsonObject()) {
                    DialogBody body = parseBody(elem.getAsJsonObject());
                    if (body != null) {
                        bodies.add(body);
                    }
                } else if (elem.isJsonPrimitive()) {
                    Component comp = MessageUtil.parse(elem.getAsString());
                    if (!comp.equals(Component.empty())) {
                        bodies.add(DialogBody.plainMessage(comp));
                    }
                }
            }
        }
        baseBuilder.body(bodies);

        DialogBase base = baseBuilder.build();
        DialogType dialogType = parseDialogType(json, confirmCommand, cancelCommand);

        return Dialog.create(factory -> factory.empty()
                .base(base)
                .type(dialogType)
        );
    }

    private static DialogBody parseBody(JsonObject obj) {
        if (obj.has("text") && !obj.has("type")) {
            String text = obj.get("text").getAsString();
            if (text == null || text.isEmpty()) {
                return null;
            }
            return DialogBody.plainMessage(MessageUtil.parse(text));
        }

        String type = obj.has("type") ? obj.get("type").getAsString() : "minecraft:plain_message";

        if (type.equals("minecraft:item")) {
            ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
            String profileName = null;

            if (obj.has("item") && obj.get("item").isJsonObject()) {
                JsonObject itemObj = obj.getAsJsonObject("item");
                String id = itemObj.has("id") ? itemObj.get("id").getAsString() : "minecraft:player_head";
                Material mat = Material.matchMaterial(id);
                if (mat != null) {
                    stack = new ItemStack(mat);
                }

                if (itemObj.has("components") && itemObj.get("components").isJsonObject()) {
                    JsonObject comps = itemObj.getAsJsonObject("components");
                    if (comps.has("minecraft:profile") && comps.get("minecraft:profile").isJsonObject()) {
                        JsonObject profile = comps.getAsJsonObject("minecraft:profile");
                        if (profile.has("name")) {
                            profileName = profile.get("name").getAsString();
                        }
                    }
                    if (comps.has("minecraft:custom_name")) {
                        Component customName = parseComponent(comps.get("minecraft:custom_name"));
                        ItemMeta meta = stack.getItemMeta();
                        if (meta != null) {
                            meta.displayName(customName);
                            stack.setItemMeta(meta);
                        }
                    }
                }
            }

            if (profileName != null) {
                if (stack.getItemMeta() instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(profileName));
                    if (!skullMeta.hasDisplayName()) {
                        skullMeta.displayName(MessageUtil.parse("<white><bold>" + profileName + "</bold></white>"));
                    }
                    stack.setItemMeta(skullMeta);
                }
            }

            ItemDialogBody.Builder itemBuilder = DialogBody.item(stack);

            if (obj.has("description")) {
                Component descComponent;
                int width = -1;
                if (obj.get("description").isJsonObject()) {
                    JsonObject descObj = obj.getAsJsonObject("description");
                    descComponent = parseComponent(descObj.has("contents") ? descObj.get("contents") : (descObj.has("text") ? descObj.get("text") : descObj));
                    if (descObj.has("width")) {
                        width = descObj.get("width").getAsInt();
                    }
                } else {
                    descComponent = parseComponent(obj.get("description"));
                }
                if (descComponent != null && !descComponent.equals(Component.empty())) {
                    PlainMessageDialogBody descBody = width > 0
                            ? DialogBody.plainMessage(descComponent, width)
                            : DialogBody.plainMessage(descComponent);
                    itemBuilder.description(descBody);
                }
            }

            if (obj.has("show_decorations")) {
                itemBuilder.showDecorations(obj.get("show_decorations").getAsBoolean());
            } else if (obj.has("showDecorations")) {
                itemBuilder.showDecorations(obj.get("showDecorations").getAsBoolean());
            } else {
                itemBuilder.showDecorations(false);
            }

            if (obj.has("show_tooltip")) {
                itemBuilder.showTooltip(obj.get("show_tooltip").getAsBoolean());
            } else if (obj.has("showTooltip")) {
                itemBuilder.showTooltip(obj.get("showTooltip").getAsBoolean());
            } else {
                itemBuilder.showTooltip(false);
            }

            if (obj.has("width")) {
                itemBuilder.width(obj.get("width").getAsInt());
            }
            if (obj.has("height")) {
                itemBuilder.height(obj.get("height").getAsInt());
            }

            return itemBuilder.build();
        }

        Component contents = parseComponent(obj.has("contents") ? obj.get("contents") : obj.get("text"));
        if (obj.has("width")) {
            return DialogBody.plainMessage(contents, obj.get("width").getAsInt());
        }
        return DialogBody.plainMessage(contents);
    }

    private static DialogType parseDialogType(JsonObject json, String confirmCommand, String cancelCommand) {
        int columns = json.has("columns") ? json.get("columns").getAsInt() : 2;

        if (json.has("buttons") && json.get("buttons").isJsonArray()) {
            JsonArray btnArray = json.getAsJsonArray("buttons");
            if (btnArray.size() >= 2) {
                ActionButton cancelButton = parseActionButton(
                        btnArray.get(0).getAsJsonObject(),
                        MessageUtil.parse("<color:red>Close</color>"),
                        cancelCommand
                );
                ActionButton confirmButton = parseActionButton(
                        btnArray.get(1).getAsJsonObject(),
                        MessageUtil.parse("<color:green>Confirm</color>"),
                        confirmCommand
                );
                return DialogType.multiAction(List.of(cancelButton, confirmButton), null, columns);
            } else if (btnArray.size() == 1) {
                ActionButton action = parseActionButton(
                        btnArray.get(0).getAsJsonObject(),
                        MessageUtil.parse("<color:green>OK</color>"),
                        confirmCommand
                );
                return DialogType.notice(action);
            }
        }

        ActionButton noButton = parseActionButton(
                json.has("no_button") ? json.getAsJsonObject("no_button") : (json.has("noButton") ? json.getAsJsonObject("noButton") : null),
                MessageUtil.parse("<color:red><bold>Close</bold></color>"),
                cancelCommand
        );

        ActionButton yesButton = parseActionButton(
                json.has("yes_button") ? json.getAsJsonObject("yes_button") : (json.has("yesButton") ? json.getAsJsonObject("yesButton") : null),
                MessageUtil.parse("<color:green><bold>Confirm</bold></color>"),
                confirmCommand
        );

        return DialogType.multiAction(List.of(noButton, yesButton), null, columns);
    }

    private static ActionButton parseActionButton(JsonObject buttonObj, Component defaultLabel, String command) {
        Component label = defaultLabel;
        Component tooltip = null;
        Integer width = 150;

        if (buttonObj != null) {
            if (buttonObj.has("label")) {
                label = parseComponent(buttonObj.get("label"));
            }
            if (buttonObj.has("tooltip")) {
                JsonElement tipElem = buttonObj.get("tooltip");
                if (tipElem.isJsonPrimitive() && !tipElem.getAsString().isEmpty()) {
                    tooltip = parseComponent(tipElem);
                } else if (tipElem.isJsonObject() || tipElem.isJsonArray()) {
                    tooltip = parseComponent(tipElem);
                }
            }
            if (buttonObj.has("width")) {
                width = buttonObj.get("width").getAsInt();
            } else if (buttonObj.has("scale")) {
                width = (int) Math.round(150 * buttonObj.get("scale").getAsDouble());
            }
        }

        ActionButton.Builder builder = ActionButton.builder(label);
        if (tooltip != null) {
            builder.tooltip(tooltip);
        }
        if (width != null) {
            builder.width(width);
        }

        if (command != null && !command.isEmpty()) {
            builder.action(DialogAction.staticAction(ClickEvent.runCommand(command)));
        }

        return builder.build();
    }

    public static Component parseComponent(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Component.empty();
        }

        if (element.isJsonPrimitive()) {
            return MessageUtil.parse(element.getAsString());
        }

        try {
            return GsonComponentSerializer.gson().deserializeFromTree(element);
        } catch (Throwable ignored) {
            return MessageUtil.parse(element.toString());
        }
    }
}
