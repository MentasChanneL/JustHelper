package com.prikolz.justhelper.commands.edit;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EICAttributes {

    private static final HashMap<String, EntityAttributeModifier.Operation> operationMap = initOperationMap();

    private static HashMap<String, EntityAttributeModifier.Operation> initOperationMap() {
        HashMap<String, EntityAttributeModifier.Operation> result = new HashMap<>();
        result.put("addition", EntityAttributeModifier.Operation.ADD_VALUE);
        result.put("multiply_total", EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        result.put("multiply_base", EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        return result;
    }

    private static final HashMap<EntityAttributeModifier.Operation, String> operationSym = initOperationSym();

    private static HashMap<EntityAttributeModifier.Operation, String> initOperationSym() {
        HashMap<EntityAttributeModifier.Operation, String> result = new HashMap<>();
        result.put( EntityAttributeModifier.Operation.ADD_VALUE, "+" );
        result.put( EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "*" );
        result.put( EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, "%" );
        return result;
    }

    private static final HashMap<String, EntityAttribute> attributeList = attributesMap();

    private static HashMap<String, EntityAttribute> attributesMap() {
        HashMap<String, EntityAttribute> result = new HashMap<>();

        result.put("max_health", EntityAttributes.GENERIC_MAX_HEALTH.value());
        result.put("follow_range", EntityAttributes.GENERIC_FOLLOW_RANGE.value());
        result.put("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE.value());
        result.put("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED.value());
        result.put("attack_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE.value());
        result.put("armor", EntityAttributes.GENERIC_ARMOR.value());
        result.put("armor_toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS.value());
        result.put("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED.value());
        result.put("luck", EntityAttributes.GENERIC_LUCK.value());
        result.put("max_absorption", EntityAttributes.GENERIC_MAX_ABSORPTION.value());
        result.put("gravity", EntityAttributes.GENERIC_GRAVITY.value());
        result.put("entity_interaction_range", EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE.value());
        result.put("block_interaction_range", EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE.value());
        result.put("step_height", EntityAttributes.GENERIC_STEP_HEIGHT.value());
        result.put("player_sneaking_speed", EntityAttributes.PLAYER_SNEAKING_SPEED.value());
        result.put("block_break_speed", EntityAttributes.PLAYER_BLOCK_BREAK_SPEED.value());
        result.put("sweeping_damage", EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO.value());
        result.put("scale", EntityAttributes.GENERIC_SCALE.value());
        result.put("attack_knockback", EntityAttributes.GENERIC_ATTACK_KNOCKBACK.value());
        result.put("fall_damage_multiplier", EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER.value());
        result.put("safe_fall_distance", EntityAttributes.GENERIC_SAFE_FALL_DISTANCE.value());

        return result;
    }

    private static final HashMap<String, AttributeModifierSlot> eqSlots = initSlots();

    private static HashMap<String, AttributeModifierSlot> initSlots() {
        HashMap<String, AttributeModifierSlot> result = new HashMap<>();

        result.put("any", AttributeModifierSlot.ANY);
        result.put("body", AttributeModifierSlot.CHEST);
        result.put("head", AttributeModifierSlot.HEAD);
        result.put("legs", AttributeModifierSlot.LEGS);
        result.put("feet", AttributeModifierSlot.FEET);
        result.put("mainhand", AttributeModifierSlot.MAINHAND);
        result.put("offhand", AttributeModifierSlot.OFFHAND);

        return result;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("attribute")
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    String name = StringArgumentType.getString(context, "name");
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    boolean del = removeAttribute(item, name);
                                    if (!del) {
                                        context.getSource().sendFeedback(Text.literal("Атрибут " + name + " не найден!").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(Text.literal("Атрибут " + name + " удален!").setStyle(JustCommand.success));
                                    EditItemCommand.setItemMainHand(item);
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("get")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    String name = StringArgumentType.getString(context, "name");
                                    HashMap<String, AttributeModifiersComponent.Entry> attributes = getAttributes(item);
                                    if(!attributes.containsKey(name)) {
                                        context.getSource().sendFeedback(Text.literal("Атрибут " + name + " не найден!").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback( getDisplayAttribute(attributes.get(name)) );
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, attributeList.keySet()))
                                        .then(ClientCommandManager.argument("slot", new VariantsArgumentType("slot.unknown", true, eqSlots.keySet()))
                                                .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg())
                                                        .then(ClientCommandManager.argument("action", new VariantsArgumentType("argument.entity.options.unknown", true, operationMap.keySet()))
                                                                .executes(context -> {
                                                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                                                    ItemStack item = EditItemCommand.getItemMainHand();
                                                                    String name = StringArgumentType.getString(context, "name");
                                                                    String id = StringArgumentType.getString(context, "id");
                                                                    String slot = VariantsArgumentType.getParameter(context, "slot");
                                                                    double value = DoubleArgumentType.getDouble(context, "value");
                                                                    String action = VariantsArgumentType.getParameter(context, "action");
                                                                    addAttribute(item, name, id, value, slot, action);
                                                                    EditItemCommand.setItemMainHand(item);
                                                                    context.getSource().sendFeedback(Text.literal("Предмету добавлен атрибут " + name).setStyle(JustCommand.success));
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(ClientCommandManager.literal("move")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                .then(ClientCommandManager.argument("move", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            if (EditItemCommand.msgItemIsNull(context)) return 0;
                                            ItemStack item = EditItemCommand.getItemMainHand();
                                            String id = StringArgumentType.getString(context, "name");
                                            int move = IntegerArgumentType.getInteger(context, "move");
                                            if(moveAttribute(item, id, move)) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Аттрибут ")
                                                                .append(Text.literal(id).setStyle(JustCommand.white))
                                                                .append(Text.literal(" перемещен на "))
                                                                .append(Text.literal("" + move).setStyle(JustCommand.white))
                                                                .append(Text.literal(" индекс"))
                                                                .setStyle(JustCommand.warn)
                                                );
                                                return 1;
                                            }else{
                                                context.getSource().sendFeedback(
                                                        Text.literal("Аттрибут ")
                                                                .append(Text.literal(id).setStyle(JustCommand.white))
                                                                .append(Text.literal(" не найден!"))
                                                                .setStyle(JustCommand.error)
                                                );
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(ClientCommandManager.literal("clear")
                        .executes(context -> {
                            if (EditItemCommand.msgItemIsNull(context)) return 0;
                            ItemStack item = EditItemCommand.getItemMainHand();
                            HashMap<String, AttributeModifiersComponent.Entry> attributes = getAttributes(item);
                            if(attributes.isEmpty()) {
                                context.getSource().sendFeedback(
                                        Text.literal("Аттрибуты не найдены!").setStyle(JustCommand.warn)
                                );
                                return 0;
                            }
                            cleatAttributes(item);
                            context.getSource().sendFeedback(
                                    Text.literal("Очищено ")
                                            .append(Text.literal(attributes.size() + "").setStyle(JustCommand.white))
                                            .append(Text.literal(" атрибутов!"))
                                            .setStyle(JustCommand.error)
                            );
                            return 1;
                        })
                )
                .executes(context -> {
                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    context.getSource().sendFeedback(Text.literal("\nСписок атрибутов:"));
                    context.getSource().sendFeedback(Text.literal("⏷"));

                    HashMap<String, AttributeModifiersComponent.Entry> attributes = getAttributes(item);

                    for (String id : attributes.keySet()) {
                        context.getSource().sendFeedback( getDisplayAttribute(attributes.get(id)) );
                    }
                    context.getSource().sendFeedback(Text.literal("⏶"));
                    return 1;
                });
        return result;
    }

    private static Text getDisplayAttribute(AttributeModifiersComponent.Entry attribute) {
        EntityAttributeModifier mod = attribute.modifier();

        String id = mod.id().getPath();
        String type = attribute.attribute().value().getTranslationKey();
        String slot = attribute.slot().asString().toLowerCase();
        String amount = String.valueOf(mod.value());
        String operation = operationSym.get(mod.operation());

        return
                Text.literal(" • ")
                        .append(Text.literal(id).setStyle(JustCommand.warn
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + id)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id))
                        ))
                        .append(Text.literal(" | ").setStyle(JustCommand.white))
                        .append(Text.translatable("item.modifiers." + slot).setStyle(JustCommand.aqua))
                        .append(Text.literal(" ").setStyle(JustCommand.white))
                        .append(Text.translatable(type).setStyle(JustCommand.gold))
                        .append(Text.literal(" " + operation + amount).setStyle(JustCommand.white));
    }

    private static HashMap<String, AttributeModifiersComponent.Entry> getAttributes(ItemStack item) {
        HashMap<String, AttributeModifiersComponent.Entry> result = new HashMap<>();
        AttributeModifiersComponent component = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (component == null) return result;
        if (component.modifiers() == null || component.modifiers().isEmpty()) return result;
        for (AttributeModifiersComponent.Entry attribute : component.modifiers()) {
            result.put(attribute.modifier().id().getPath(), attribute);
        }
        return result;
    }

    private static boolean removeAttribute(ItemStack item, String name) {
        AttributeModifiersComponent component = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (component == null) return false;
        if (component.modifiers() == null) return false;
        List<AttributeModifiersComponent.Entry> attributes = new LinkedList<>(component.modifiers());
        AtomicBoolean result = new AtomicBoolean(false);
        attributes.removeIf(entry -> {
            if (name.equals( entry.modifier().id().getPath() )) {
                result.set(true);
                return true;
            }
            return false;
        });
        AttributeModifiersComponent news = new AttributeModifiersComponent(attributes, true);
        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, news);
        return result.get();
    }

    private static void addAttribute(ItemStack item, String name, String id, double amount, String slot, String operation ) {
        removeAttribute(item, name);
        AttributeModifiersComponent attributes = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        List<AttributeModifiersComponent.Entry> list = new LinkedList<>();
        if(attributes != null) list.addAll(attributes.modifiers());
        RegistryEntry<EntityAttribute> registryEntry = Registries.ATTRIBUTE.getEntry( attributeList.get(id) );
        EntityAttributeModifier modifier = new EntityAttributeModifier(Identifier.of(name), amount, operationMap.get(operation));
        list.add( new AttributeModifiersComponent.Entry( registryEntry, modifier, eqSlots.get(slot)) );
        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(list, true));
    }

    private static boolean moveAttribute(ItemStack item, String name, int index) {
        AttributeModifiersComponent attributes = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if(attributes == null) return false;
        int entryIndex = -1;
        AttributeModifiersComponent.Entry entry = null;
        for(int i = 0; i < attributes.modifiers().size(); i++) {
            entry = attributes.modifiers().get(i);
            if(entry.modifier().id().getPath().equals(name)) {
                entryIndex = i;
                break;
            }
        }
        if(entryIndex == -1) return false;
        int move = index % attributes.modifiers().size();
        attributes.modifiers().remove(entryIndex);
        if(move >= attributes.modifiers().size()) {
            attributes.modifiers().add(entry);
        }else{
            attributes.modifiers().add(move, entry);
        }
        return true;
    }

    private static void cleatAttributes(ItemStack item) {
        item.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    }

}
