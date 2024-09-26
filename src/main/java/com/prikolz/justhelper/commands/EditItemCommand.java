package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.commands.argumens.ColorArgumentType;
import com.prikolz.justhelper.commands.argumens.TextFormattingArgumentType;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import com.prikolz.justhelper.vars.text.VarText;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditItemCommand {
    private static final HashMap<String, String> operationSym = initOperationSym();

    private static HashMap<String, String> initOperationSym() {
        HashMap<String, String> result = new HashMap<>();
        result.put("addition", "+");
        result.put("multiply_total", "*");
        result.put("multiply_base", "%");
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

        return result;
    }

    private static Set<String> enchantList = Set.of();

    private static Set<String> initEnchant() {
        Set<String> result = new HashSet<>();

        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return result;

        Registry<Enchantment> reg = client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

        for(RegistryKey<Enchantment> key : reg.getKeys() ) {
            result.add(key.getValue().getPath());
        }

        return result;
    }

    private static final HashMap<String, Item> materialMap = initMaterialMap();

    private static HashMap<String, Item> initMaterialMap() {
        HashMap<String, Item> result = new HashMap<>();

        for(RegistryKey<Item> key : Registries.ITEM.getKeys()) {
            result.put( key.getValue().getPath(), Registries.ITEM.get(key) );
        }

        return result;
    }

    private static final Set<String> potionList = initPotion();

    private static Set<String> initPotion() {
        Set<String> result = new HashSet<>();

        for(RegistryKey<StatusEffect> key : Registries.STATUS_EFFECT.getKeys()) {
            result.add(key.getValue().getPath());
        }

        return result;
    }

    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal( Config.getCommandName("edit") )
                        .then( ClientCommandManager.literal("tag" )
                                .then(ClientCommandManager.literal("add")
                                        .then( ClientCommandManager.argument("name", StringArgumentType.string())
                                                .then( ClientCommandManager.argument("value", StringArgumentType.greedyString() )
                                                        .executes(context -> {
                                                            if( msgItemIsNull(context) ) return 0;
                                                            ItemStack item = getItemMainHand();
                                                            String key = StringArgumentType.getString(context, "name");
                                                            String value = StringArgumentType.getString(context, "value");
                                                            for(char c : key.toCharArray()) {
                                                                if( !(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') && c != '_' && c != '-') {
                                                                    context.getSource().sendFeedback(Text.literal("JustHelper > Название тега содержит недопустимые символы! Название может содержать только: маленькие латинские(английские) буквы, цифры, нижнее подчеркивание или тире.").setStyle(JustCommand.error));
                                                                    return 0;
                                                                }
                                                            }
                                                            addItemTag(item, key, value);
                                                            context.getSource().sendFeedback(
                                                                    Text.literal("")
                                                                            .append(Text.literal("Предмету установлен тег ").setStyle(JustCommand.success))
                                                                            .append(Text.literal(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                            .append(Text.literal(" со значением ").setStyle(JustCommand.success))
                                                                            .append(Text.literal(value).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                            );
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then( ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String key = StringArgumentType.getString(context, "name");
                                                    boolean deleted = removeItemTag(item, key);
                                                    if(deleted) {
                                                        context.getSource().sendFeedback(
                                                                Text.literal("")
                                                                        .append(Text.literal("Тег ").setStyle(JustCommand.success))
                                                                        .append(Text.literal(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                        .append(Text.literal(" удален!").setStyle(JustCommand.success))
                                                        );
                                                        return 1;
                                                    }
                                                    context.getSource().sendFeedback(
                                                            Text.literal("")
                                                                    .append(Text.literal("Тег ").setStyle(JustCommand.warn))
                                                                    .append(Text.literal(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                    .append(Text.literal(" не установлен!").setStyle(JustCommand.warn))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("get")
                                        .then( ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String key = StringArgumentType.getString(context, "name");
                                                    HashMap<String, String> tags = getItemTags(item);
                                                    if(!tags.containsKey(key)) {
                                                        context.getSource().sendFeedback(Text.literal("JustHelper > Тег не найден!").setStyle(JustCommand.warn));
                                                        return 0;
                                                    }
                                                    String value = tags.get(key);
                                                    if(value.length() > 10) value = value.substring(0, 10) + "...";
                                                    context.getSource().sendFeedback(
                                                            Text.literal( key ).setStyle(Style.EMPTY
                                                                            .withColor(Formatting.YELLOW)
                                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key))
                                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать ключ\n" + key))))
                                                                    .append(Text.literal(" = ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                    .append(Text.literal(value).setStyle(Style.EMPTY
                                                                            .withColor(Formatting.GOLD)
                                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать значение\n" + value)))))
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    HashMap<String, String> tags = getItemTags(item);
                                    if(tags.isEmpty()) {
                                        context.getSource().sendFeedback(Text.literal("JustHelper > Теги не найдены").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(Text.literal("\nУстановленные теги предмета:\n⏷"));
                                    for(String key : tags.keySet()) {
                                        String value = tags.get(key);
                                        String cutValue = value;
                                        if(cutValue.length() > 10) cutValue = cutValue.substring(0, 10) + "...";
                                        context.getSource().sendFeedback(
                                                Text.literal(" • ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                        .append(key).setStyle(Style.EMPTY
                                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать ключ\n" + key)))
                                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key))
                                                                .withColor(Formatting.YELLOW)
                                                        )
                                                        .append(Text.literal(" = ").setStyle(Style.EMPTY
                                                                .withColor(Formatting.WHITE)))
                                                        .append(Text.literal(cutValue).setStyle(Style.EMPTY
                                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать значение\n" + value)))
                                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                                                                .withColor(Formatting.GOLD)
                                                        ))
                                        );
                                    }
                                    context.getSource().sendFeedback(Text.literal("⏶"));
                                    return 1;
                                })
                        )
                        .then( ClientCommandManager.literal("rename" )
                                .then(ClientCommandManager.argument("format", new TextFormattingArgumentType())
                                        .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String name = StringArgumentType.getString(context, "text");
                                                    VarText.TextType type = TextFormattingArgumentType.getFormatType(context, "format");
                                                    VarText text = VarText.getText(name, type);
                                                    setItemMainHand( setItemName(text.toJson(), item) );
                                                    context.getSource().sendFeedback(
                                                            Text.literal("Заданно имя предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                                    .append(item.getName().copy().setStyle( item.getName().getStyle()
                                                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Копировать: " + name)))
                                                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, name))
                                                                    ))
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    context.getSource().sendFeedback(
                                            Text.literal("Текущее имя предмета: ")
                                                    .append(item.getName())
                                    );
                                    return 1;
                                })

                        )
                        .then(ClientCommandManager.literal("attribute")
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    String name = StringArgumentType.getString(context, "name");
                                                    ItemStack item = getItemMainHand();
                                                    boolean del = removeAttribute(item, name);
                                                    if(!del) {
                                                        context.getSource().sendFeedback(Text.literal("Атрибут " + name + " не найден!").setStyle(JustCommand.warn));
                                                        return 0;
                                                    }
                                                    context.getSource().sendFeedback(Text.literal("Атрибут " + name + " удален!").setStyle(JustCommand.success));
                                                    setItemMainHand(item);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("get")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    if (msgItemIsNull(context)) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String name = StringArgumentType.getString(context, "name");
                                                    AttributeModifiersComponent component = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                                                    Text errorNotFound = Text.literal("Атрибут " + name + " не найден!").setStyle(JustCommand.warn);
                                                    if (component == null) {
                                                        context.getSource().sendFeedback(errorNotFound);
                                                        return 0;
                                                    }
                                                    Text out = null;
                                                    for (AttributeModifiersComponent.Entry attributeEntry: component.modifiers()) {
                                                        RegistryEntry<EntityAttribute> attribute = attributeEntry.attribute();
                                                        EntityAttributeModifier modifier = attributeEntry.modifier();
                                                        AttributeModifierSlot slot = attributeEntry.slot();

                                                        if (name.equals(attribute.getIdAsString())) {
                                                            out = Text.literal(" • ")
                                                                    .append(Text.literal(attribute.getIdAsString()).setStyle(JustCommand.warn
                                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + attribute.getIdAsString())))
                                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, attribute.getIdAsString()))
                                                                    ))
                                                                    .append(Text.literal(" | ").setStyle(JustCommand.white))
                                                                    .append(Text.translatable("item.modifiers." + slot.name()).setStyle(JustCommand.aqua))
                                                                    .append(Text.literal(" ").setStyle(JustCommand.white))
                                                                    .append(Text.translatable(attribute.value().getTranslationKey()).setStyle(JustCommand.gold))
                                                                    .append(Text.literal(" " + operationSym.get(modifier.operation().asString()) + modifier.value()).setStyle(JustCommand.white));
                                                        }
                                                    }
                                                    if (out == null) {
                                                        context.getSource().sendFeedback(errorNotFound);
                                                        return 0;
                                                    }
                                                    context.getSource().sendFeedback(out);

                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, attributeList.keySet()))
                                                        .then(ClientCommandManager.argument("slot", new VariantsArgumentType("slot.unknown", true, "chest", "mainhand", "offhand", "head", "legs", "feet"))
                                                                .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg())
                                                                        .then(ClientCommandManager.argument("action", new VariantsArgumentType("argument.entity.options.unknown", true, operationSym.keySet() ))
                                                                                .executes(context -> {
                                                                                    if (msgItemIsNull(context)) return 0;
                                                                                    ItemStack item = getItemMainHand();
                                                                                    String name = StringArgumentType.getString(context, "name");
                                                                                    removeAttribute(item, name);
                                                                                    String id = StringArgumentType.getString(context, "id");
                                                                                    String slot = VariantsArgumentType.getParameter(context, "slot");
                                                                                    double value = DoubleArgumentType.getDouble(context, "value");
                                                                                    String action = VariantsArgumentType.getParameter(context, "action");
                                                                                    EntityAttribute attribute = attributeList.get(id);
                                                                                    EntityAttributeModifier mod = new EntityAttributeModifier(Identifier.of(name), value, EntityAttributeModifier.Operation.valueOf(action.toUpperCase()));
                                                                                    EquipmentSlot equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
                                                                                    AttributeModifiersComponent attributeModifiersComponent = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                                                                                    if (attributeModifiersComponent == null) {
                                                                                        attributeModifiersComponent = new AttributeModifiersComponent(new LinkedList<>(), true);
                                                                                    }
                                                                                    Registry<EntityAttribute> registry = Registries.ATTRIBUTE;
                                                                                    RegistryEntry<EntityAttribute> registryEntry = registry.getEntry(attribute);
                                                                                    attributeModifiersComponent.modifiers().add(
                                                                                            new AttributeModifiersComponent.Entry(registryEntry, mod, AttributeModifierSlot.forEquipmentSlot(equipmentSlot))
                                                                                    );
                                                                                    item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributeModifiersComponent);
                                                                                    setItemMainHand(item);
                                                                                    context.getSource().sendFeedback(Text.literal("Предмету добавлен атрибут " + name).setStyle(JustCommand.success));
                                                                                    return 1;
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .executes(context -> {
                                    if (msgItemIsNull(context)) return 0;
                                    ItemStack item = getItemMainHand();
                                    context.getSource().sendFeedback(Text.literal(""));
                                    context.getSource().sendFeedback(Text.literal("Список атрибутов:"));
                                    context.getSource().sendFeedback(Text.literal("⏷"));
                                    List<AttributeModifiersComponent.Entry> attributes = new LinkedList<>();
                                    AttributeModifiersComponent component = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                                    if (component != null) {
                                        attributes = component.modifiers();
                                    }

                                    for (AttributeModifiersComponent.Entry attribute : attributes) {
                                        EntityAttributeModifier mod = attribute.modifier();
                                        RegistryEntry<EntityAttribute> attributeRegistryEntry = attribute.attribute();
                                        AttributeModifierSlot slot = attribute.slot();

                                        context.getSource().sendFeedback(
                                                Text.literal(" • ")
                                                        .append(Text.literal(attributeRegistryEntry.getIdAsString()).setStyle(JustCommand.warn
                                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + attributeRegistryEntry.getIdAsString())))
                                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, attributeRegistryEntry.getIdAsString()))
                                                        ))
                                                        .append(Text.literal(" | ").setStyle(JustCommand.white))
                                                        .append(Text.translatable("item.modifiers." + slot.name()).setStyle(JustCommand.aqua))
                                                        .append(Text.literal(" ").setStyle(JustCommand.white))
                                                        .append(Text.translatable(attributeRegistryEntry.value().getTranslationKey()).setStyle(JustCommand.gold))
                                                        .append(Text.literal(" " + operationSym.get(mod.operation().asString()) + mod.value()).setStyle(JustCommand.white))
                                        );
                                    }
                                    context.getSource().sendFeedback(Text.literal("⏶"));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("model")
                                .then(ClientCommandManager.argument("number", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            if (msgItemIsNull(context)) return 0;
                                            ItemStack item = getItemMainHand();
                                            int number = IntegerArgumentType.getInteger(context, "data");
                                            CustomModelDataComponent component = new CustomModelDataComponent(number);
                                            item.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Установлена модель предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                            .append(Text.literal(String.valueOf(number)).setStyle(JustCommand.warn))
                                            );
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    if (msgItemIsNull(context)) return 0;
                                    ItemStack item = getItemMainHand();
                                    int data = 0;
                                    CustomModelDataComponent component = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
                                    if (component != null) {
                                        data = component.value();
                                    }
                                    context.getSource().sendFeedback(
                                            Text.literal("Текущая модель предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                    .append(Text.literal(data + "").setStyle(JustCommand.warn))
                                    );
                                    return data;
                                })
                        )
                        //.then(ClientCommandManager.literal("flag")
                        //        .then(ClientCommandManager.literal("add")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, flagsList.keySet()))
                        //                        .executes(context -> {
                        //                            if( msgItemIsNull(context) ) return 0;
                        //                            ItemStack item = getItemMainHand();
                        //                            String id = VariantsArgumentType.getParameter(context, "id");
                        //                            boolean added = addFlag(item, id);
                        //                            if(!added) {
                        //                                context.getSource().sendFeedback(Text.literal("Флаг " + id + " уже установлен").setStyle(JustCommand.warn));
                        //                                return 0;
                        //                            }
                        //                            setItemMainHand(item);
                        //                            context.getSource().sendFeedback(Text.literal("Флаг " + id + " добавлен!").setStyle(JustCommand.sucsess));
                        //                            return 1;
                        //                        })
                        //                )
                        //        )
                        //        .then(ClientCommandManager.literal("remove")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, flagsList.keySet()))
                        //                        .executes(context -> {
                        //                            if( msgItemIsNull(context) ) return 0;
                        //                            ItemStack item = getItemMainHand();
                        //                            String id = VariantsArgumentType.getParameter(context, "id");
                        //                            boolean removed = removeFlag(item, id);
                        //                            if(!removed) {
                        //                                context.getSource().sendFeedback(Text.literal("Флаг " + id + " не установлен").setStyle(JustCommand.warn));
                        //                                return 0;
                        //                            }
                        //                            setItemMainHand(item);
                        //                            context.getSource().sendFeedback(Text.literal("Флаг " + id + " удален!").setStyle(JustCommand.sucsess));
                        //                            return 1;
                        //                        })
                        //                )
                        //        )
                        //        .executes(context -> {
                        //            if( msgItemIsNull(context) ) return 0;
                        //            ItemStack item = getItemMainHand();
                        //            Set<String> flags = getFlags(item);
                        //            if(flags.isEmpty()) {
                        //                context.getSource().sendFeedback(Text.literal("Флаги не установлены").setStyle(JustCommand.warn));
                        //                return 0;
                        //            }
                        //            context.getSource().sendFeedback(Text.literal("\nУстановленные флаги скрытия:\n⏷"));
                        //            for (String key : flags) {
                        //                context.getSource().sendFeedback(Text.literal(" • " + translatedFlags.get(key)).setStyle(JustCommand.warn));
                        //            }
                        //            context.getSource().sendFeedback(Text.literal("⏶"));
                        //            return 1;
                        //        })
                        //)

                        .then(ClientCommandManager.literal("lore")
                                .then(ClientCommandManager.literal("clear")
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            clearLore(item);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(Text.literal("Описание очищено").setStyle(JustCommand.warn));
                                            return 1;
                                        })
                                )
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("format", new TextFormattingArgumentType())
                                                .then(ClientCommandManager.argument("lines", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            if( msgItemIsNull(context) ) return 0;
                                                            ItemStack item = getItemMainHand();
                                                            String line = StringArgumentType.getString(context, "lines");
                                                            VarText.TextType type = TextFormattingArgumentType.getFormatType(context, "format");
                                                            String[] lines = line.split("\\\\n");
                                                            String err = addLoreLines(item, 0, VarText.getTexts(lines, type), true);
                                                            if(!err.isEmpty()) {
                                                                context.getSource().sendFeedback(Text.literal(err).setStyle(JustCommand.error));
                                                                return 0;
                                                            }
                                                            context.getSource().sendFeedback(Text.literal("Добавлены новые строчки").setStyle(JustCommand.success));
                                                            setItemMainHand(item);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("insert")
                                        .then(ClientCommandManager.argument("line", IntegerArgumentType.integer())
                                                .then(ClientCommandManager.argument("format", new TextFormattingArgumentType())
                                                        .then(ClientCommandManager.argument("lines", StringArgumentType.greedyString())
                                                                .executes(context -> {
                                                                    if( msgItemIsNull(context) ) return 0;
                                                                    ItemStack item = getItemMainHand();
                                                                    String line = StringArgumentType.getString(context, "lines");
                                                                    VarText.TextType type = TextFormattingArgumentType.getFormatType(context, "format");
                                                                    int pos = IntegerArgumentType.getInteger(context, "line");
                                                                    String[] lines = line.split("\\\\n");
                                                                    String err = addLoreLines(item, pos, VarText.getTexts(lines, type), true);
                                                                    if(!err.isEmpty()) {
                                                                        context.getSource().sendFeedback(Text.literal(err).setStyle(JustCommand.error));
                                                                        return 0;
                                                                    }
                                                                    context.getSource().sendFeedback(Text.literal("Вставлены новые строчки").setStyle(JustCommand.success));
                                                                    setItemMainHand(item);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("set")
                                        .then(ClientCommandManager.argument("line", IntegerArgumentType.integer())
                                                .then(ClientCommandManager.argument("format", new TextFormattingArgumentType())
                                                        .then(ClientCommandManager.argument("lines", StringArgumentType.greedyString())
                                                                .executes(context -> {
                                                                    if( msgItemIsNull(context) ) return 0;
                                                                    ItemStack item = getItemMainHand();
                                                                    String line = StringArgumentType.getString(context, "lines");
                                                                    VarText.TextType type = TextFormattingArgumentType.getFormatType(context, "format");
                                                                    int pos = IntegerArgumentType.getInteger(context, "line");
                                                                    String[] lines = line.split("\\\\n");
                                                                    String err = addLoreLines(item, pos, VarText.getTexts(lines, type), false);
                                                                    if(!err.isEmpty()) {
                                                                        context.getSource().sendFeedback(Text.literal(err).setStyle(JustCommand.error));
                                                                        return 0;
                                                                    }
                                                                    context.getSource().sendFeedback(Text.literal("Строчки заменены").setStyle(JustCommand.success));
                                                                    setItemMainHand(item);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("line", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    int pos = IntegerArgumentType.getInteger(context, "line");
                                                    String err = removeLoreLine(item,pos);
                                                    if(!err.isEmpty()) {
                                                        context.getSource().sendFeedback(Text.literal(err).setStyle(JustCommand.warn));
                                                        return 0;
                                                    }
                                                    setItemMainHand(item);
                                                    if(pos == 0) {
                                                        context.getSource().sendFeedback(Text.literal("Последняя строчка удалена").setStyle(JustCommand.success));
                                                        return 1;
                                                    }
                                                    context.getSource().sendFeedback(Text.literal("Строчка " + pos + " удалена").setStyle(JustCommand.success));
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(ClientCommandManager.literal("count")
                                .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            int count = IntegerArgumentType.getInteger(context, "count");
                                            item.setCount(count);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Установлено количество: ").setStyle(JustCommand.white)
                                                            .append(Text.literal(count + "").setStyle(JustCommand.warn))
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("color")
                                .then(ClientCommandManager.argument("color", new ColorArgumentType())
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            int color = ColorArgumentType.getParameter(context, "color");
                                            String hex = StringArgumentType.getString(context, "color");
                                            setColor(item, color);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Предмету установлен ").setStyle(JustCommand.white)
                                                            .append(Text.literal("цвет").setStyle(Style.EMPTY
                                                                    .withColor(color)
                                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("#" + hex.toUpperCase()).setStyle(Style.EMPTY.withColor(color))))
                                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "#" + hex.toUpperCase()))
                                                            ))
                                            );
                                            return 1;
                                        })
                                )
                        )

                        //.then(ClientCommandManager.literal("enchantment")
                        //        .then(ClientCommandManager.literal("add")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, enchantList))
                        //                        .then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255))
                        //                                .executes(context -> {
                        //                                    if( msgItemIsNull(context) ) return 0;
                        //                                    ItemStack item = getItemMainHand();
                        //                                    String key = VariantsArgumentType.getParameter(context, "id");
                        //                                    short level = (short) IntegerArgumentType.getInteger(context,  "level");
                        //                                    addEnchant(item, "minecraft:" + key, level);
                        //                                    context.getSource().sendFeedback(
                        //                                            Text.literal("Добавлено зачарование ").setStyle(JustCommand.white)
                        //                                                    .append(Text.literal(key).setStyle(JustCommand.warn))
                        //                                                    .append(Text.literal(" с уровнем ").setStyle(JustCommand.white))
                        //                                                    .append(Text.literal(level + "").setStyle(JustCommand.warn))
                        //                                    );
                        //                                    return 1;
                        //                                })
                        //                        )
                        //                )
                        //        )
                        //        .then(ClientCommandManager.literal("remove")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, enchantList))
                        //                        .executes(context -> {
                        //                            if( msgItemIsNull(context) ) return 0;
                        //                            ItemStack item = getItemMainHand();
                        //                            String key = VariantsArgumentType.getParameter(context, "id");
                        //                            boolean removed = removeEnchant(item, "minecraft:" + key);
                        //                            if(!removed) {
                        //                                context.getSource().sendFeedback(
                        //                                        Text.literal("Зачарование ").setStyle(JustCommand.warn)
                        //                                                .append(Text.literal(key).setStyle(JustCommand.white))
                        //                                                .append(Text.literal(" не установлено!").setStyle(JustCommand.warn))
                        //                                );
                        //                                return  0;
                        //                            }
                        //                            context.getSource().sendFeedback(
                        //                                    Text.literal("Зачарование ").setStyle(JustCommand.sucsess)
                        //                                            .append(Text.literal(key).setStyle(JustCommand.white))
                        //                                            .append(Text.literal(" удалено!").setStyle(JustCommand.sucsess))
                        //                            );
                        //                            return 1;
                        //                        })
                        //                )
                        //        )
                        //        .then(ClientCommandManager.literal("clear")
                        //                .executes(context -> {
                        //                    if( msgItemIsNull(context) ) return 0;
                        //                    ItemStack item = getItemMainHand();
                        //                    clearEnchants(item);
                        //                    context.getSource().sendFeedback(
                        //                            Text.literal("Зачарования удалены").setStyle(JustCommand.sucsess)
                        //                    );
                        //                    return 1;
                        //                })
                        //        )
                        //        .executes(context -> {
                        //            if( msgItemIsNull(context) ) return 0;
                        //            ItemStack item = getItemMainHand();
                        //            HashMap<String, Short> enchs = getEnchants(item);
                        //            if(enchs.isEmpty()) {
                        //                context.getSource().sendFeedback(Text.literal("Зачарования не установлены!").setStyle(JustCommand.warn));
                        //                return 0;
                        //            }
                        //            context.getSource().sendFeedback(Text.literal("\nЗачарования предмета:\n⏷"));
                        //            for (String key : enchs.keySet()) {
                        //                context.getSource().sendFeedback(
                        //                        Text.literal(" • ").setStyle(JustCommand.warn)
                        //                                .append(Text.translatable("enchantment.minecraft." + key).setStyle(JustCommand.white))
                        //                                .append(Text.literal(" " + enchs.get(key)))
                        //                );
                        //            }
                        //            context.getSource().sendFeedback(Text.literal("⏶"));
                        //            return enchs.size();
                        //        })
                        //)

                        .then(ClientCommandManager.literal("material")
                                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, materialMap.keySet()))
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            String id = VariantsArgumentType.getParameter(context, "id");
                                            setItemMainHand( setItemMaterial(getItemMainHand(), id) );
                                            context.getSource().sendFeedback(
                                                    Text.literal("Тип предмета установлен на ").setStyle(JustCommand.white)
                                                            .append(Text.literal(id).setStyle(JustCommand.success))
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("unbreakable")
                                .then(ClientCommandManager.argument("enable", new VariantsArgumentType("Ожидалось true/false", false, "true", "false"))
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            String mode = VariantsArgumentType.getParameter(context, "enable");
                                            boolean set = false;
                                            if(mode.equals("true")) set = true;
                                            setItemUnbreakable(item, set);
                                            setItemMainHand(item);
                                            if(set) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Неразрушаемость предмета").setStyle(JustCommand.white)
                                                                .append(Text.literal(" включена").setStyle(JustCommand.success))
                                                );
                                                return 1;
                                            }
                                            context.getSource().sendFeedback(
                                                    Text.literal("Неразрушаемость предмета").setStyle(JustCommand.white)
                                                            .append(Text.literal(" выключена").setStyle(JustCommand.warn))
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("damage")
                                .then(ClientCommandManager.literal("set").then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            item.setDamage(amount);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Установлено повреждение предмета: ").setStyle(JustCommand.white)
                                                            .append(Text.literal("" + amount).setStyle(JustCommand.success))
                                            );
                                            return 1;
                                        }))
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    int amount = item.getDamage();
                                    context.getSource().sendFeedback(
                                            Text.literal("Текущее повреждение предмета: ").setStyle(JustCommand.white)
                                                    .append(Text.literal("" + amount).setStyle(JustCommand.warn))
                                    );
                                    return 1;
                                })
                        )

                        //.then(ClientCommandManager.literal("potion")
                        //        .then(ClientCommandManager.literal("add")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                        //                        .then(ClientCommandManager.argument("amplifier", IntegerArgumentType.integer(0, 256))
                        //                                .then(ClientCommandManager.argument("duration", IntegerArgumentType.integer(0))
                        //                                        .executes(context -> {
                        //                                            if( msgItemIsNull(context) ) return 0;
                        //                                            ItemStack item = getItemMainHand();
                        //                                            String id = VariantsArgumentType.getParameter(context, "id");
                        //                                            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
                        //                                            int duration = IntegerArgumentType.getInteger(context, "duration");
                        //                                            setPotionEffect(id, amplifier, duration, item);
                        //                                            setItemMainHand(item);
                        //                                            context.getSource().sendFeedback(
                        //                                                    Text.literal("Предмету установлен эффект зелья ").setStyle(JustCommand.white)
                        //                                                            .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.sucsess))
                        //                                                            .append(Text.literal(" силой "))
                        //                                                            .append(Text.literal("" + amplifier).setStyle(JustCommand.sucsess))
                        //                                                            .append(Text.literal(" длительностью в "))
                        //                                                            .append(Text.literal("" + duration).setStyle(JustCommand.sucsess))
                        //                                            );
                        //                                            return 1;
                        //                                        })
                        //                                )
                        //                        )
                        //                )
                        //        )
                        //        .then(ClientCommandManager.literal("remove")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                        //                        .executes(context -> {
                        //                            if( msgItemIsNull(context) ) return 0;
                        //                            ItemStack item = getItemMainHand();
                        //                            String id = VariantsArgumentType.getParameter(context, "id");
                        //                            boolean result = removePotionEffect(id, item);
                        //                            if(result) {
                        //                                setItemMainHand(item);
                        //                                context.getSource().sendFeedback(
                        //                                        Text.literal("Удален эффект зелья ").setStyle(JustCommand.white)
                        //                                                .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.sucsess))
                        //                                );
                        //                                return 1;
                        //                            }
                        //                            context.getSource().sendFeedback(
                        //                                    Text.literal("Эффект ").setStyle(JustCommand.warn)
                        //                                            .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.white))
                        //                                            .append(Text.literal(" не найден"))
                        //                            );
                        //                            return 0;
                        //                        })
                        //                )
                        //        )
                        //        .then(ClientCommandManager.literal("get")
                        //                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                        //                        .executes(context -> {
                        //                            if( msgItemIsNull(context) ) return 0;
                        //                            ItemStack item = getItemMainHand();
                        //                            String id = VariantsArgumentType.getParameter(context, "id");
                        //                            HashMap<String, PotionData> data = getPotionEffects(item);
                        //                            if(!(data.containsKey(id))) {
                        //                                context.getSource().sendFeedback(
                        //                                        Text.literal("Эффект ").setStyle(JustCommand.warn)
                        //                                                .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.white))
                        //                                                .append(Text.literal(" не найден"))
                        //                                );
                        //                                return 0;
                        //                            }
                        //                            PotionData pd = data.get(id);
                        //                            context.getSource().sendFeedback(
                        //                                    Text.literal(" • ").setStyle(JustCommand.white)
                        //                                            .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.warn))
                        //                                            .append(Text.literal(" " + pd.amplifier).setStyle(JustCommand.gold))
                        //                                            .append(Text.literal(" | "))
                        //                                            .append(Text.literal("" + pd.duration).setStyle(JustCommand.gold)));
                        //                            return 1;
                        //                        })
                        //                )
//
                        //        )
                        //        .executes(context -> {
                        //            if( msgItemIsNull(context) ) return 0;
                        //            ItemStack item = getItemMainHand();
                        //            HashMap<String, PotionData> data = getPotionEffects(item);
                        //            context.getSource().sendFeedback(
                        //                    Text.literal("\nУстановленные эффекты:\n⏷").setStyle(JustCommand.white)
                        //            );
                        //            for(String id : data.keySet()) {
                        //                PotionData pd = data.get(id);
                        //                context.getSource().sendFeedback(
                        //                        Text.literal(" • ").setStyle(JustCommand.white)
                        //                                .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.warn))
                        //                                .append(Text.literal(" " + pd.amplifier).setStyle(JustCommand.gold))
                        //                                .append(Text.literal(" | "))
                        //                                .append(Text.literal("" + pd.duration).setStyle(JustCommand.gold))
                        //                );
                        //            }
                        //            context.getSource().sendFeedback(
                        //                    Text.literal("⏶\n").setStyle(JustCommand.white)
                        //            );
                        //            return 1;
                        //        })
                        //)

                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > Аргументы команды edit:").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                                            .append( Text.literal("\n\ntag - Добавить/Удалить/Получить кастомный тег предмета").setStyle(JustCommand.gold))
                                            .append( Text.literal("\n\nrename - Изменить имя предмета. Поддерживаются коды цветов &, плейсхолдер %space%, формат json и формат minimessage").setStyle(JustCommand.gold))
                                            .append( Text.literal("\n\nmodel - Изменить/Получить модель предмета(CustomModelData)").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\nattribute - Добавляет/Удаляет/Получает атрибуты предмета.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\nflag - Добавляет/Удаляет/Получает флаги скрытия предмета.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\nlore - Позволяет изменять описание предмета.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\ncolor - Установить цвет предмета. Например, для кожанной брони.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\nunbreakable - Включить/Выключить неразрушаемость предмета.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\nmaterial - Установить тип предмета.").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\n\npotion - Добавляет/Удаляет/Получает эффекты зелий предмета.").setStyle(JustCommand.gold) )
                            );
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }

    private static boolean msgItemIsNull(CommandContext<FabricClientCommandSource> context) {
        if(getItemMainHand() != null && !getItemMainHand().isEmpty()) return false;
        context.getSource().sendFeedback(Text.literal("JustHelper > Не получилось редактировать. Возьмите предмет в руку!").setStyle(JustCommand.error));
        return true;
    }

    private static ItemStack getItemMainHand() {
        try {
            return MinecraftClient.getInstance().player.getInventory().getStack(MinecraftClient.getInstance().player.getInventory().selectedSlot);
        }catch (Exception e) {
            return null;
        }
    }

    private static void setItemMainHand(ItemStack item) {
        try {
            MinecraftClient.getInstance().player.getInventory().setStack(MinecraftClient.getInstance().player.getInventory().selectedSlot, item);
        }catch (Exception ignore) {
        }
    }

    private static boolean removeAttribute(ItemStack item, String name) {
        AtomicBoolean result = new AtomicBoolean(false);
        AttributeModifiersComponent component = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (component == null) return false;
        List<AttributeModifiersComponent.Entry> attributes = component.modifiers();
        if (attributes == null || attributes.isEmpty()) return false;
        attributes.removeIf(entry -> {
            if (name.replace("minecraft:", "").equals(entry.attribute().getIdAsString())) {
                result.set(true);
                return true;
            }
            return false;
        });

        return result.get();
    }

    //private static Set<String> getFlags(ItemStack item) {
    //    Set<String> result = new HashSet<>();
    //    NbtCompound nbt = item.getNbt();
    //    if(nbt == null) {
    //        return result;
    //    }
    //    int flags = nbt.getInt("HideFlags");
    //    if(flags == 0) {
    //        return result;
    //    }
    //    int current = 128;
    //    int check;
    //    int i = 0;
    //    while(flags > 0) {
    //        check = flags - current;
    //        if(check > -1) {
    //            flags = check;
    //            result.add(flagsIdList[i]);
    //        }
    //        i++;
    //        if(i == flagsIdList.length) break;
    //        current /= 2;
    //    }
    //    return result;
    //}

    //private static boolean removeFlag(ItemStack item, String key) {
    //    if( !getFlags(item).contains(key) ) return false;
    //    NbtCompound nbt = item.getNbt();
    //    nbt.putInt("HideFlags", nbt.getInt("HideFlags") - flagsList.get(key).getFlag());
    //    item.setNbt(nbt);
    //    return true;
    //}
//
    //private static boolean addFlag(ItemStack item, String key) {
    //    if( getFlags(item).contains(key) ) return false;
    //    NbtCompound nbt = item.getNbt();
    //    if(nbt == null) nbt = new NbtCompound();
    //    nbt.putInt("HideFlags", nbt.getInt("HideFlags") + flagsList.get(key).getFlag());
    //    item.setNbt(nbt);
    //    return true;
    //}

    private static String addLoreLines(ItemStack item, int pos, VarText[] lines, boolean isNew) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return "Что-то пошло не так во время выполнения команды. Находитесь ли вы в мире на данный момент?";
        if (pos < 0) return "Номер линии должен быть больше -1!";
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        List<Text> list = new LinkedList<>();
        if (loreComponent != null) {
            list = loreComponent.styledLines();
        }
        if(isNew) {
            if(pos == 0) {
                pos = list.size();
            }else {
                pos -= 1;
                while(pos > list.size()) {
                    list.add(Text.literal(" "));
                }
            }

            for (VarText line : lines) {
                try {
                    list.add(pos, Text.Serialization.fromJson(line.toJson(), world.getRegistryManager()));
                }catch (Exception e) {e.printStackTrace();}
                pos++;
            }

        }else{
            if(pos == 0) {
                pos = list.size() - 1;
                if(pos < 0) pos = 0;
            }else {
                pos -= 1;
                while(pos + lines.length > list.size()) {
                    list.add(Text.literal(" "));
                }
            }
            for (VarText line : lines) {
                list.set(pos, Text.Serialization.fromJson( line.toJson(), world.getRegistryManager() ));
                pos++;
            }
        }
        item.set(DataComponentTypes.LORE, new LoreComponent(list));
        return "";
    }

    private static void clearLore(@NotNull ItemStack item) {
        item.remove(DataComponentTypes.LORE);
    }

    private static @NotNull String removeLoreLine(@NotNull ItemStack item, int line) {
        if (line < 0) return "Номер линии должен быть больше -1!";
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent == null) return "Описание не заданно";
        List<Text> lines = loreComponent.styledLines();
        if (line > lines.size()) return "Линия ещё не задана";
        if (line == 0) line = lines.size();
        line -= 1;
        lines.remove(line);
        item.set(DataComponentTypes.LORE, new LoreComponent(lines));
        return "";
    }

    private static void setColor(@NotNull ItemStack item, int color) {
        item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
    }

    private static void addEnchant(@NotNull ItemStack item, String key, short level) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world != null) {
            EnchantmentHelper.apply(item, builder -> {
                Registry<Enchantment> registry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
                Optional<RegistryEntry.Reference<Enchantment>> registryEntry = registry.getEntry(Identifier.ofVanilla(key));
                registryEntry.ifPresent(enchantmentReference -> builder.set(enchantmentReference, level));
            });
        }
    }

    private static boolean removeEnchant(@NotNull ItemStack item, @NotNull String key) {
        ItemEnchantmentsComponent component = item.get(DataComponentTypes.ENCHANTMENTS);
        if (component != null && !component.isEmpty()) {
            ItemEnchantmentsComponent.Builder newComponent = new ItemEnchantmentsComponent.Builder(component);
            for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
                if (key.equals(enchantment.getIdAsString().replace("minecraft:", ""))) {
                    newComponent.remove(enchantmentRegistryEntry -> enchantmentRegistryEntry.equals(enchantment));
                    item.set(DataComponentTypes.ENCHANTMENTS, newComponent.build());
                    return true;
                }
            }
        }
        return false;
    }

    private static void clearEnchants(@NotNull ItemStack item) {
        item.remove(DataComponentTypes.ENCHANTMENTS);
    }

    private static @NotNull HashMap<String, Short> getEnchants(@NotNull ItemStack item) {
        HashMap<String, Short> result = new HashMap<>();
        ItemEnchantmentsComponent component = item.get(DataComponentTypes.ENCHANTMENTS);
        if (component == null || component.isEmpty()) return result;
        for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
            result.put(enchantment.getIdAsString().replace("minecraft:", ""), (short) component.getLevel(enchantment));
        }
        return result;
    }

    private static @NotNull ItemStack setItemMaterial(@NotNull ItemStack item, String material) {
        ItemStack newItem = new ItemStack(materialMap.get(material));
        ComponentMap map = item.getComponents();
        newItem.applyComponentsFrom(map);
        return newItem;
    }

    private static void setItemUnbreakable(@NotNull ItemStack item, boolean on) {
        item.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(on));
    }

    private static void addItemTag(@NotNull ItemStack item, String key, String value) {
        NbtComponent nbtComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt;
        if (nbtComponent == null) {
            nbt = new NbtCompound();
        } else {
            nbt = nbtComponent.copyNbt();
        }
        NbtCompound values = nbt.getCompound("PublicBukkitValues");
        values.putString("justcreativeplus:" + key, value);
        nbt.put("PublicBukkitValues", values);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }


    private static boolean removeItemTag(@NotNull ItemStack item, String key) {
        NbtComponent nbtComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return false;
        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null) return false;
        NbtCompound values = nbt.getCompound("PublicBukkitValues");
        if (values.isEmpty() || !values.contains("justcreativeplus:" + key)) return false;
        values.remove("justcreativeplus:" + key);
        nbt.put("PublicBukkitValues", values);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return true;
    }

    private static @NotNull HashMap<String, String> getItemTags(@NotNull ItemStack item) {
        HashMap<String, String> result = new HashMap<>();
        NbtComponent nbtComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            NbtCompound nbt = nbtComponent.copyNbt();
            if (nbt == null) return result;
            NbtCompound values = nbt.getCompound("PublicBukkitValues");
            if (values.isEmpty()) return result;
            for (String key : values.getKeys()) {
                if (!key.startsWith("justcreativeplus")) continue;
                String value = values.getString(key);
                result.put(key.substring(17), value);
            }
        }
        return result;
    }

    private static ItemStack setItemName(String json, ItemStack item) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return item;
        item.set(DataComponentTypes.ITEM_NAME, Text.Serialization.fromJson(json, world.getRegistryManager()));
        return item;
    }

    private static void setPotionEffect(String id, int amplifier, int duration, ItemStack item) {
        //removePotionEffect(id, item);
        //NbtCompound nbt = item.getNbt();
        //if(nbt == null) nbt = new NbtCompound();
        //NbtList custom = nbt.getList("custom_potion_effects", 10);
        //NbtCompound potion = new NbtCompound();
        //potion.putString("id", "minecraft:" + id);
        //potion.putByte("amplifier", (byte) amplifier);
        //potion.putInt("duration", duration);
        //custom.add(potion);
        //nbt.put("custom_potion_effects", custom);
        //item.setNbt(nbt);
    }

    private static boolean removePotionEffect(String id, ItemStack item) {
        //NbtCompound nbt = item.getNbt();
        //if(nbt == null) return false;
        //NbtList custom = nbt.getList("custom_potion_effects", 10);
        //if(custom.isEmpty()) return false;
        //int i = 0;
        //while(true) {
        //    NbtCompound element = custom.getCompound(i);
        //    if(element.getString("id").equals("minecraft:" + id)) {
        //        custom.remove(i);
        //        break;
        //    }
        //    i++;
        //    if(i >= custom.size()) {
        //        return false;
        //    }
        //}
        //nbt.put("custom_potion_effects", custom);
        //item.setNbt(nbt);
        return true;
    }

    private static HashMap<String, PotionData> getPotionEffects(ItemStack item) {
        HashMap<String, PotionData> result = new HashMap<>();
        //NbtCompound nbt = item.getNbt();
        //if(nbt == null) return result;
        //NbtList custom = nbt.getList("custom_potion_effects", 10);
        //if(custom.isEmpty()) return result;
        //for(NbtElement el : custom) {
        //    if(!(el instanceof NbtCompound potion)) continue;
        //    String id = potion.getString("id").replace("minecraft:", "");
        //    byte amplifier = potion.getByte("amplifier");
        //    int duration = potion.getInt("duration");
        //    result.put(id, new PotionData(duration, amplifier));
        //}
        return result;
    }

    private record PotionData(int duration, byte amplifier) {}
}
