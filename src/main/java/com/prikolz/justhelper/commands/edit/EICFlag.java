package com.prikolz.justhelper.commands.edit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

import java.util.LinkedList;
import java.util.List;

public class EICFlag {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("hide_flags")
                .then(ClientCommandManager.literal("tooltip")
                        .then(ClientCommandManager.argument("hide", new VariantsArgumentType("Ожидалось true или false", false, "true", "false"))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    boolean hide = VariantsArgumentType.getParameter(context, "hide").equals("true");
                                    hideTooltip(item, hide);
                                    if(hide) {
                                        context.getSource().sendFeedback(Text.literal("Флаги предмета скрыты").setStyle(JustCommand.gold));
                                    }else{
                                        context.getSource().sendFeedback(Text.literal("Флаги предмета отображены").setStyle(JustCommand.warn));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("additional_tooltip")
                        .then(ClientCommandManager.argument("hide", new VariantsArgumentType("Ожидалось true или false", false, "true", "false"))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    boolean hide = VariantsArgumentType.getParameter(context, "hide").equals("true");
                                    hideAdditionalTooltip(item, hide);
                                    if(hide) {
                                        context.getSource().sendFeedback(Text.literal("Дополнительные флаги предмета скрыты").setStyle(JustCommand.gold));
                                    }else{
                                        context.getSource().sendFeedback(Text.literal("Дополнительные флаги предмета отображены").setStyle(JustCommand.warn));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("attributes")
                        .then(ClientCommandManager.argument("hide", new VariantsArgumentType("Ожидалось true или false", false, "true", "false"))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    boolean hide = VariantsArgumentType.getParameter(context, "hide").equals("true");
                                    hideAttributes(item, hide);
                                    if(hide) {
                                        context.getSource().sendFeedback(Text.literal("Атрибуты предмета скрыты").setStyle(JustCommand.gold));
                                    }else{
                                        context.getSource().sendFeedback(Text.literal("Атрибуты предмета отображены").setStyle(JustCommand.warn));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("enchantments")
                        .then(ClientCommandManager.argument("hide", new VariantsArgumentType("Ожидалось true или false", false, "true", "false"))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    boolean hide = VariantsArgumentType.getParameter(context, "hide").equals("true");
                                    hideEnchantments(item, hide);
                                    if(hide) {
                                        context.getSource().sendFeedback(Text.literal("Зачарования предмета скрыты").setStyle(JustCommand.gold));
                                    }else{
                                        context.getSource().sendFeedback(Text.literal("Зачарования предмета отображены").setStyle(JustCommand.warn));
                                    }
                                    return 1;
                                })
                        )
                )
                .executes(context -> {
                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    String has1 = item.get(DataComponentTypes.HIDE_TOOLTIP) == null ? "Отображены" : "Скрыты";
                    String has2 = item.get(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP) == null ? "Отображены" : "Скрыты";
                    boolean h3 = false;
                    AttributeModifiersComponent attributes = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if(attributes != null && attributes.showInTooltip()) h3 = true;
                    String has3 = h3 ? "Отображены" : "Скрыты";
                    boolean h4 = false;
                    ItemEnchantmentsComponent enchs = item.get(DataComponentTypes.ENCHANTMENTS);
                    if(enchs != null && enchs.toString().contains("showInTooltip=true")) h4 = true;
                    String has4 = h4 ? "Отображены" : "Скрыты";
                    context.getSource().sendFeedback(
                            Text.literal("Флаги: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has1).setStyle(JustCommand.white))
                    );
                    context.getSource().sendFeedback(
                            Text.literal("Дополнительные флаги: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has2).setStyle(JustCommand.white))
                    );
                    context.getSource().sendFeedback(
                            Text.literal("Атрибуты: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has3).setStyle(JustCommand.white))
                    );
                    context.getSource().sendFeedback(
                            Text.literal("Зачарования: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has4).setStyle(JustCommand.white))
                    );
                    return 1;
                });
        return result;
    }

    private static void hideTooltip(ItemStack item, boolean hide) {
        if(hide) {
            item.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
            return;
        }
        item.remove(DataComponentTypes.HIDE_TOOLTIP);
    }
    private static void hideAdditionalTooltip(ItemStack item, boolean hide) {
        if(hide) {
            item.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            return;
        }
        item.remove(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
    }
    private static void hideAttributes(ItemStack item, boolean hide) {
        AttributeModifiersComponent attributes = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if(attributes == null) { attributes = new AttributeModifiersComponent(new LinkedList<>(), !hide); }
        else { attributes = attributes.withShowInTooltip(!hide); }
        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);
    }
    private static void hideEnchantments(ItemStack item, boolean hide) {
        ItemEnchantmentsComponent enchs = item.get(DataComponentTypes.ENCHANTMENTS);
        if(enchs == null) enchs = ItemEnchantmentsComponent.DEFAULT;
        enchs = enchs.withShowInTooltip(!hide);
        item.set(DataComponentTypes.ENCHANTMENTS, enchs);
    }
}
