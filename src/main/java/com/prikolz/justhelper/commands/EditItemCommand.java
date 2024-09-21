package com.prikolz.justhelper.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.prikolz.justhelper.commands.argumens.ColorArgumentType;
import com.prikolz.justhelper.commands.argumens.DisplayJSONArgumentType;
import com.prikolz.justhelper.commands.argumens.TextFormattingArgumentType;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import com.prikolz.justhelper.vars.text.VarText;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;

public class EditItemCommand {
    private static final Set<Character> tagWhitelist = initWL();
    private static final HashMap<String, String> operationSym = initOperationSym();

    private static Set<Character> initWL() {
        Set<Character> result = new HashSet<>();

        result.add('a'); result.add('0');
        result.add('b'); result.add('1');
        result.add('c'); result.add('2');
        result.add('d'); result.add('3');
        result.add('e'); result.add('4');
        result.add('f'); result.add('5');
        result.add('g'); result.add('6');
        result.add('h'); result.add('7');
        result.add('i'); result.add('8');
        result.add('j'); result.add('9');
        result.add('k'); result.add('-');
        result.add('l'); result.add('_');
        result.add('m');
        result.add('n');
        result.add('o');
        result.add('p');
        result.add('q');
        result.add('r');
        result.add('s');
        result.add('t');
        result.add('u');
        result.add('v');
        result.add('w');
        result.add('x');
        result.add('y');
        result.add('z');

        return result;
    }

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

        result.put("max_health", EntityAttributes.GENERIC_MAX_HEALTH);
        result.put("follow_range", EntityAttributes.GENERIC_FOLLOW_RANGE);
        result.put("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        result.put("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED);
        result.put("attack_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE);
        result.put("armor", EntityAttributes.GENERIC_ARMOR);
        result.put("armor_toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        result.put("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED);
        result.put("luck", EntityAttributes.GENERIC_LUCK);
        result.put("max_absorption", EntityAttributes.GENERIC_MAX_ABSORPTION);

        return result;
    }

    private static final String[] flagsIdList = new String[]{
            "ArmorTrim",
            "Dyed",
            "HideOthers",
            "CanPlaceOn",
            "CanDestroy",
            "Unbreakable",
            "Modifiers",
            "Enchantments"
    };

    private static final HashMap<String, ItemStack.TooltipSection> flagsList = flagsInit();

    private static HashMap<String, ItemStack.TooltipSection> flagsInit() {
        HashMap<String, ItemStack.TooltipSection> result = new HashMap<>();

        result.put("Enchantments", ItemStack.TooltipSection.ENCHANTMENTS);
        result.put("Modifiers", ItemStack.TooltipSection.MODIFIERS);
        result.put("Unbreakable", ItemStack.TooltipSection.UNBREAKABLE);
        result.put("CanDestroy", ItemStack.TooltipSection.CAN_DESTROY);
        result.put("CanPlaceOn", ItemStack.TooltipSection.CAN_PLACE);
        result.put("HideOthers", ItemStack.TooltipSection.ADDITIONAL);
        result.put("Dyed", ItemStack.TooltipSection.DYE);
        result.put("ArmorTrim", ItemStack.TooltipSection.UPGRADES);

        return result;
    }

    private static final HashMap<String, String> translatedFlags = translateFlags();

    private static HashMap<String, String> translateFlags() {
        HashMap<String, String> result = new HashMap<>();

        result.put("Enchantments", "Зачарования");
        result.put("Modifiers", "Модификаторы");
        result.put("Unbreakable", "Неразрушаемость");
        result.put("CanDestroy", "Может сломать...");
        result.put("CanPlaceOn", "Можно поставить на...");
        result.put("HideOthers", "Прочее");
        result.put("Dyed", "Цвет");
        result.put("ArmorTrim", "Улучшения брони");

        return result;
    }

    private static final Set<String> enchantList = initEnchant();

    private static Set<String> initEnchant() {
        Set<String> result = new HashSet<>();

        for(RegistryKey<Enchantment> key : Registries.ENCHANTMENT.getKeys()) {
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
                ClientCommandManager.literal("edit")
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
                                                                if(!tagWhitelist.contains(c)) {
                                                                    context.getSource().sendFeedback(Text.literal("JustHelper > Название тега содержит недопустимые символы! Название может содержать только: маленькие латинские(английские) буквы, цифры, нижнее подчеркивание или тире.").setStyle(JustCommand.error));
                                                                    return 0;
                                                                }
                                                            }
                                                            addItemTag(item, key, value);
                                                            context.getSource().sendFeedback(
                                                                    Text.literal("")
                                                                            .append(Text.literal("Предмету установлен тег ").setStyle(JustCommand.sucsess))
                                                                            .append(Text.literal(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                            .append(Text.literal(" со значением ").setStyle(JustCommand.sucsess))
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
                                                                        .append(Text.literal("Тег ").setStyle(JustCommand.sucsess))
                                                                        .append(Text.literal(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                        .append(Text.literal(" удален!").setStyle(JustCommand.sucsess))
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
                                                    context.getSource().sendFeedback(Text.literal("Атрибут " + name + " удален!").setStyle(JustCommand.sucsess));
                                                    setItemMainHand(item);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("get")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String name = StringArgumentType.getString(context, "name");
                                                    String[] slots = new String[]{"CHEST", "MAINHAND", "OFFHAND", "FEET", "HEAD", "LEGS"};
                                                    Text out = null;
                                                    found: for(String slot : slots) {
                                                        EquipmentSlot eq = EquipmentSlot.valueOf(slot);
                                                        Multimap<EntityAttribute, EntityAttributeModifier> map = item.getAttributeModifiers(eq);
                                                        if(map.isEmpty()) continue;
                                                        for(EntityAttribute attribute : map.keys()) {
                                                            Collection<EntityAttributeModifier> mods = map.get(attribute);
                                                            for(EntityAttributeModifier mod : mods) {
                                                                if(mod.getName().equals(name)) {
                                                                    out = Text.literal(" • ")
                                                                            .append(Text.literal(mod.getName()).setStyle(JustCommand.warn
                                                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + mod.getName())))
                                                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, mod.getName()))
                                                                            ))
                                                                            .append(Text.literal(" | ").setStyle(JustCommand.white))
                                                                            .append(Text.translatable("item.modifiers." + eq.getName()).setStyle(JustCommand.aqua))
                                                                            .append(Text.literal(" ").setStyle(JustCommand.white))
                                                                            .append(Text.translatable(attribute.getTranslationKey()).setStyle(JustCommand.gold))
                                                                            .append(Text.literal(" " + operationSym.get(mod.getOperation().asString()) + mod.getValue()).setStyle(JustCommand.white));
                                                                    break found;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if(out == null) {
                                                        context.getSource().sendFeedback(Text.literal("Атрибут " + name + " не найден!").setStyle(JustCommand.warn));
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
                                                                                    if( msgItemIsNull(context) ) return 0;
                                                                                    ItemStack item = getItemMainHand();
                                                                                    String name = StringArgumentType.getString(context, "name");
                                                                                    removeAttribute(item, name);
                                                                                    String id = StringArgumentType.getString(context, "id");
                                                                                    String slot = VariantsArgumentType.getParameter(context, "slot");
                                                                                    double value = DoubleArgumentType.getDouble(context, "value");
                                                                                    String action = VariantsArgumentType.getParameter(context, "action");

                                                                                    EntityAttribute attribute = attributeList.get(id);
                                                                                    EntityAttributeModifier mod = new EntityAttributeModifier(name, value, EntityAttributeModifier.Operation.valueOf(action.toUpperCase()));
                                                                                    EquipmentSlot eqslot = EquipmentSlot.valueOf(slot.toUpperCase());
                                                                                    item.addAttributeModifier(attribute, mod, eqslot);
                                                                                    setItemMainHand(item);
                                                                                    context.getSource().sendFeedback(Text.literal("Предмету добавлен атрибут " + name).setStyle(JustCommand.sucsess));

                                                                                    return 1;
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    String[] slots = new String[]{"CHEST", "MAINHAND", "OFFHAND", "FEET", "HEAD", "LEGS"};
                                    context.getSource().sendFeedback(Text.literal(""));
                                    context.getSource().sendFeedback(Text.literal("Список атрибутов:"));
                                    context.getSource().sendFeedback(Text.literal("⏷"));
                                    for(String slot : slots) {
                                        EquipmentSlot eq = EquipmentSlot.valueOf(slot);
                                        Multimap<EntityAttribute, EntityAttributeModifier> map = item.getAttributeModifiers(eq);
                                        if(map.isEmpty()) continue;
                                        for(EntityAttribute attribute : map.keys()) {
                                            Collection<EntityAttributeModifier> mods = map.get(attribute);
                                            for(EntityAttributeModifier mod : mods) {
                                                context.getSource().sendFeedback(
                                                        Text.literal(" • ")
                                                                .append(Text.literal(mod.getName()).setStyle(JustCommand.warn
                                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + mod.getName())))
                                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, mod.getName()))
                                                                 ))
                                                                .append(Text.literal(" | ").setStyle(JustCommand.white))
                                                                .append(Text.translatable("item.modifiers." + eq.getName()).setStyle(JustCommand.aqua))
                                                                .append(Text.literal(" ").setStyle(JustCommand.white))
                                                                .append(Text.translatable(attribute.getTranslationKey()).setStyle(JustCommand.gold))
                                                                .append(Text.literal(" " + operationSym.get(mod.getOperation().asString()) + mod.getValue()).setStyle(JustCommand.white))
                                                );
                                            }
                                        }
                                    }
                                    context.getSource().sendFeedback(Text.literal("⏶"));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("model")
                                .then(ClientCommandManager.argument("number", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            int arg = IntegerArgumentType.getInteger(context, "number");
                                            NbtCompound nbt = item.getNbt();
                                            if(nbt == null) nbt = new NbtCompound();
                                            nbt.putInt("CustomModelData", arg);
                                            item.setNbt(nbt);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Установлена модель предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                            .append(Text.literal(arg + "").setStyle(JustCommand.warn))
                                            );
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    NbtCompound nbt = item.getNbt();
                                    if(nbt == null) nbt = new NbtCompound();
                                    int cmd = nbt.getInt("CustomModelData");
                                    context.getSource().sendFeedback(
                                            Text.literal("Текущая модель предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                    .append(Text.literal(cmd + "").setStyle(JustCommand.warn))
                                    );
                                    return cmd;
                                })
                        )
                        .then(ClientCommandManager.literal("flag")
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, flagsList.keySet()))
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String id = VariantsArgumentType.getParameter(context, "id");
                                                    boolean added = addFlag(item, id);
                                                    if(!added) {
                                                        context.getSource().sendFeedback(Text.literal("Флаг " + id + " уже установлен").setStyle(JustCommand.warn));
                                                        return 0;
                                                    }
                                                    setItemMainHand(item);
                                                    context.getSource().sendFeedback(Text.literal("Флаг " + id + " добавлен!").setStyle(JustCommand.sucsess));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, flagsList.keySet()))
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String id = VariantsArgumentType.getParameter(context, "id");
                                                    boolean removed = removeFlag(item, id);
                                                    if(!removed) {
                                                        context.getSource().sendFeedback(Text.literal("Флаг " + id + " не установлен").setStyle(JustCommand.warn));
                                                        return 0;
                                                    }
                                                    setItemMainHand(item);
                                                    context.getSource().sendFeedback(Text.literal("Флаг " + id + " удален!").setStyle(JustCommand.sucsess));
                                                    return 1;
                                                })
                                        )
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    Set<String> flags = getFlags(item);
                                    if(flags.isEmpty()) {
                                        context.getSource().sendFeedback(Text.literal("Флаги не установлены").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(Text.literal("\nУстановленные флаги скрытия:\n⏷"));
                                    for (String key : flags) {
                                        context.getSource().sendFeedback(Text.literal(" • " + translatedFlags.get(key)).setStyle(JustCommand.warn));
                                    }
                                    context.getSource().sendFeedback(Text.literal("⏶"));
                                    return 1;
                                })
                        )

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
                                                            context.getSource().sendFeedback(Text.literal("Добавлены новые строчки").setStyle(JustCommand.sucsess));
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
                                                                    context.getSource().sendFeedback(Text.literal("Вставлены новые строчки").setStyle(JustCommand.sucsess));
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
                                                                    context.getSource().sendFeedback(Text.literal("Строчки заменены").setStyle(JustCommand.sucsess));
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
                                                        context.getSource().sendFeedback(Text.literal("Последняя строчка удалена").setStyle(JustCommand.sucsess));
                                                        return 1;
                                                    }
                                                    context.getSource().sendFeedback(Text.literal("Строчка " + pos + " удалена").setStyle(JustCommand.sucsess));
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

                        .then(ClientCommandManager.literal("enchantment")
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, enchantList))
                                                .then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255))
                                                        .executes(context -> {
                                                            if( msgItemIsNull(context) ) return 0;
                                                            ItemStack item = getItemMainHand();
                                                            String key = VariantsArgumentType.getParameter(context, "id");
                                                            short level = (short) IntegerArgumentType.getInteger(context,  "level");
                                                            addEnchant(item, "minecraft:" + key, level);
                                                            context.getSource().sendFeedback(
                                                                    Text.literal("Добавлено зачарование ").setStyle(JustCommand.white)
                                                                            .append(Text.literal(key).setStyle(JustCommand.warn))
                                                                            .append(Text.literal(" с уровнем ").setStyle(JustCommand.white))
                                                                            .append(Text.literal(level + "").setStyle(JustCommand.warn))
                                                            );
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, enchantList))
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String key = VariantsArgumentType.getParameter(context, "id");
                                                    boolean removed = removeEnchant(item, "minecraft:" + key);
                                                    if(!removed) {
                                                        context.getSource().sendFeedback(
                                                                Text.literal("Зачарование ").setStyle(JustCommand.warn)
                                                                        .append(Text.literal(key).setStyle(JustCommand.white))
                                                                        .append(Text.literal(" не установлено!").setStyle(JustCommand.warn))
                                                        );
                                                        return  0;
                                                    }
                                                    context.getSource().sendFeedback(
                                                            Text.literal("Зачарование ").setStyle(JustCommand.sucsess)
                                                                    .append(Text.literal(key).setStyle(JustCommand.white))
                                                                    .append(Text.literal(" удалено!").setStyle(JustCommand.sucsess))
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("clear")
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            clearEnchants(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Зачарования удалены").setStyle(JustCommand.sucsess)
                                            );
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    HashMap<String, Short> enchs = getEnchants(item);
                                    if(enchs.isEmpty()) {
                                        context.getSource().sendFeedback(Text.literal("Зачарования не установлены!").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(Text.literal("\nЗачарования предмета:\n⏷"));
                                    for (String key : enchs.keySet()) {
                                        context.getSource().sendFeedback(
                                                Text.literal(" • ").setStyle(JustCommand.warn)
                                                        .append(Text.translatable("enchantment.minecraft." + key).setStyle(JustCommand.white))
                                                        .append(Text.literal(" " + enchs.get(key)))
                                        );
                                    }
                                    context.getSource().sendFeedback(Text.literal("⏶"));
                                    return enchs.size();
                                })
                        )

                        .then(ClientCommandManager.literal("material")
                                .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, materialMap.keySet()))
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            String id = VariantsArgumentType.getParameter(context, "id");
                                            setItemMainHand( setItemMaterial(getItemMainHand(), id) );
                                            context.getSource().sendFeedback(
                                                    Text.literal("Тип предмета установлен на ").setStyle(JustCommand.white)
                                                            .append(Text.literal(id).setStyle(JustCommand.sucsess))
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
                                                                .append(Text.literal(" включена").setStyle(JustCommand.sucsess))
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
                                                            .append(Text.literal("" + amount).setStyle(JustCommand.sucsess))
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

                        .then(ClientCommandManager.literal("potion")
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                                                .then(ClientCommandManager.argument("amplifier", IntegerArgumentType.integer(0, 256))
                                                        .then(ClientCommandManager.argument("duration", IntegerArgumentType.integer(0))
                                                                .executes(context -> {
                                                                    if( msgItemIsNull(context) ) return 0;
                                                                    ItemStack item = getItemMainHand();
                                                                    String id = VariantsArgumentType.getParameter(context, "id");
                                                                    int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
                                                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                                                    setPotionEffect(id, amplifier, duration, item);
                                                                    setItemMainHand(item);
                                                                    context.getSource().sendFeedback(
                                                                            Text.literal("Предмету установлен эффект зелья ").setStyle(JustCommand.white)
                                                                                    .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.sucsess))
                                                                                    .append(Text.literal(" силой "))
                                                                                    .append(Text.literal("" + amplifier).setStyle(JustCommand.sucsess))
                                                                                    .append(Text.literal(" длительностью в "))
                                                                                    .append(Text.literal("" + duration).setStyle(JustCommand.sucsess))
                                                                    );
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String id = VariantsArgumentType.getParameter(context, "id");
                                                    boolean result = removePotionEffect(id, item);
                                                    if(result) {
                                                        setItemMainHand(item);
                                                        context.getSource().sendFeedback(
                                                                Text.literal("Удален эффект зелья ").setStyle(JustCommand.white)
                                                                        .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.sucsess))
                                                        );
                                                        return 1;
                                                    }
                                                    context.getSource().sendFeedback(
                                                            Text.literal("Эффект ").setStyle(JustCommand.warn)
                                                                    .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.white))
                                                                    .append(Text.literal(" не найден"))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("get")
                                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList))
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String id = VariantsArgumentType.getParameter(context, "id");
                                                    HashMap<String, PotionData> data = getPotionEffects(item);
                                                    if(!(data.containsKey(id))) {
                                                        context.getSource().sendFeedback(
                                                                Text.literal("Эффект ").setStyle(JustCommand.warn)
                                                                        .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.white))
                                                                        .append(Text.literal(" не найден"))
                                                        );
                                                        return 0;
                                                    }
                                                    PotionData pd = data.get(id);
                                                    context.getSource().sendFeedback(
                                                            Text.literal(" • ").setStyle(JustCommand.white)
                                                                    .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.warn))
                                                                    .append(Text.literal(" " + pd.amplifier).setStyle(JustCommand.gold))
                                                                    .append(Text.literal(" | "))
                                                                    .append(Text.literal("" + pd.duration).setStyle(JustCommand.gold)));
                                                    return 1;
                                                })
                                        )

                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    HashMap<String, PotionData> data = getPotionEffects(item);
                                    context.getSource().sendFeedback(
                                            Text.literal("\nУстановленные эффекты:\n⏷").setStyle(JustCommand.white)
                                    );
                                    for(String id : data.keySet()) {
                                        PotionData pd = data.get(id);
                                        context.getSource().sendFeedback(
                                                Text.literal(" • ").setStyle(JustCommand.white)
                                                        .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.warn))
                                                        .append(Text.literal(" " + pd.amplifier).setStyle(JustCommand.gold))
                                                        .append(Text.literal(" | "))
                                                        .append(Text.literal("" + pd.duration).setStyle(JustCommand.gold))
                                        );
                                    }
                                    context.getSource().sendFeedback(
                                            Text.literal("⏶\n").setStyle(JustCommand.white)
                                    );
                                    return 1;
                                })
                        )

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
        boolean result = false;

        NbtCompound nbt = item.getNbt();
        if(nbt == null) return false;
        NbtList nbtList = nbt.getList("AttributeModifiers", 10);
        if(nbtList.isEmpty()) return false;
        int i = 0;
        while(i < nbtList.size()) {
            NbtCompound attribute = nbtList.getCompound(i);
            if(attribute.getString("AttributeName").equals(name)) {
                nbtList.remove(i);
                result = true;
                continue;
            }
            i++;
        }
        if(!result) return result;
        nbt.put("AttributeModifiers", nbtList);
        item.setNbt(nbt);
        return true;
    }

    private static Set<String> getFlags(ItemStack item) {
        Set<String> result = new HashSet<>();
        NbtCompound nbt = item.getNbt();
        if(nbt == null) {
            return result;
        }
        int flags = nbt.getInt("HideFlags");
        if(flags == 0) {
            return result;
        }
        int current = 128;
        int check;
        int i = 0;
        while(flags > 0) {
            check = flags - current;
            if(check > -1) {
                flags = check;
                result.add(flagsIdList[i]);
            }
            i++;
            if(i == flagsIdList.length) break;
            current /= 2;
        }
        return result;
    }

    private static boolean removeFlag(ItemStack item, String key) {
        if( !getFlags(item).contains(key) ) return false;
        NbtCompound nbt = item.getNbt();
        nbt.putInt("HideFlags", nbt.getInt("HideFlags") - flagsList.get(key).getFlag());
        item.setNbt(nbt);
        return true;
    }

    private static boolean addFlag(ItemStack item, String key) {
        if( getFlags(item).contains(key) ) return false;
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        nbt.putInt("HideFlags", nbt.getInt("HideFlags") + flagsList.get(key).getFlag());
        item.setNbt(nbt);
        return true;
    }

    private static String addLoreLines(ItemStack item, int pos, VarText[] lines, boolean isNew) {
        if(pos < 0) return "Номер линии должен быть больше -1!";
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        NbtCompound display = nbt.getCompound("display");
        NbtList list = display.getList("Lore", NbtElement.STRING_TYPE);
        if(isNew) {
            if(pos == 0) {
                pos = list.size();
            }else {
                pos -= 1;
                while(pos > list.size()) {
                    list.add(NbtString.of("{\"text\":\"\"}"));
                }
            }

            for (VarText line : lines) {
                list.add(pos, NbtString.of( line.toJson() ));
                pos++;
            }

        }else{
            if(pos == 0) {
                pos = list.size() - 1;
                if(pos < 0) pos = 0;
            }else {
                pos -= 1;
                while(pos + lines.length > list.size()) {
                    list.add(NbtString.of("{\"text\":\"\"}"));
                }
            }
            for (VarText line : lines) {
                list.set(pos, NbtString.of( line.toJson() ));
                pos++;
            }
        }
        display.put("Lore", list);
        nbt.put("display", display);
        item.setNbt(nbt);
        return "";
    }

    private static void clearLore(ItemStack item) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return;
        NbtCompound display = nbt.getCompound("display");
        if(display.isEmpty()) return;
        display.remove("Lore");
        nbt.put("display", display);
        item.setNbt(nbt);
    }

    private static String removeLoreLine(ItemStack item, int line) {
        if(line < 0) return "Номер линии должен быть больше -1!";
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return "Описание не заданно";
        NbtCompound display = nbt.getCompound("display");
        if(display.isEmpty()) return "Описание не заданно";
        NbtList list = display.getList("Lore", NbtElement.STRING_TYPE);
        if(line > list.size()) return "Линия еще не задана";
        if(line == 0) line = list.size();
        line -= 1;
        list.remove(line);
        display.put("Lore", list);
        nbt.put("display", display);
        item.setNbt(nbt);
        return "";
    }

    private static void setColor(ItemStack item, int color) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        NbtCompound display = nbt.getCompound("display");
        display.putInt("color", color);
        nbt.put("display", display);
        item.setNbt(nbt);
    }

    private static void addEnchant(ItemStack item, String key, short level) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        removeEnchant(item, key);
        NbtList enchantments = nbt.getList("Enchantments", 10);
        NbtCompound enchant = new NbtCompound();
        enchant.putString("id", key);
        enchant.putShort("lvl", level);
        enchantments.add(enchant);
        nbt.put("Enchantments", enchantments);
        item.setNbt(nbt);
    }

    private static boolean removeEnchant(ItemStack item, String key) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return false;
        NbtList enchantments = nbt.getList("Enchantments", 10);
        String id;
        for(int i = 0; i < enchantments.size(); i++) {
            id = enchantments.getCompound(i).getString("id");
            if(id.equals(key)) {
                enchantments.remove(i);
                nbt.put("Enchantments", enchantments);
                item.setNbt(nbt);
                return true;
            }
        }
        return false;
    }

    private static void clearEnchants(ItemStack item) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return;
        nbt.remove("Enchantments");
        item.setNbt(nbt);
    }

    private static HashMap<String, Short> getEnchants(ItemStack item) {
        HashMap<String, Short> result = new HashMap<>();

        NbtCompound nbt = item.getNbt();
        if(nbt == null) return result;
        NbtList enchantments = nbt.getList("Enchantments", 10);
        if(enchantments.isEmpty()) return result;
        NbtCompound enchant;
        for(int i = 0; i < enchantments.size(); i++) {
            enchant = enchantments.getCompound(i);
            if(enchant.isEmpty()) continue;
            result.put(enchant.getString("id").replace("minecraft:", ""), enchant.getShort("lvl"));
        }

        return result;
    }

    private static ItemStack setItemMaterial(ItemStack item, String material) {
        ItemStack newItem = new ItemStack(materialMap.get(material));
        NbtCompound nbt = item.getNbt();
        if(nbt != null) newItem.setNbt(nbt);
        return newItem;
    }

    private static void setItemUnbreakable(ItemStack item, boolean on) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        byte set = 1;
        if(!on) set = 0;
        nbt.putByte("Unbreakable", set);
        item.setNbt(nbt);
    }

    private static void addItemTag(ItemStack item, String key, String value) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        NbtCompound values = nbt.getCompound("PublicBukkitValues");
        values.putString("justcreativeplus:" + key, value);
        nbt.put("PublicBukkitValues", values);
        item.setNbt(nbt);
    }

    private static boolean removeItemTag(ItemStack item, String key) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return false;
        NbtCompound values = nbt.getCompound("PublicBukkitValues");
        if(values.isEmpty() || !values.contains("justcreativeplus:" + key)) return false;
        values.remove("justcreativeplus:" + key);
        nbt.put("PublicBukkitValues", values);
        item.setNbt(values);
        return true;
    }

    private static HashMap<String, String> getItemTags(ItemStack item) {
        HashMap<String, String> result = new HashMap<>();
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return result;
        NbtCompound values = nbt.getCompound("PublicBukkitValues");
        if(values.isEmpty()) return result;
        for(String key : values.getKeys()) {
            if(!key.startsWith("justcreativeplus")) continue;
            String value = values.getString(key);
            result.put(key.substring(17), value);
        }
        return result;
    }

    private static ItemStack setItemName(String json, ItemStack item) {
        NbtCompound itemNbt = item.getNbt();
        if(itemNbt == null) itemNbt = new NbtCompound();
        NbtCompound display = itemNbt.getCompound("display");
        display.putString("Name", json);
        System.out.println(display.asString());
        itemNbt.put("display", display);
        System.out.println(itemNbt.asString());
        item.setNbt(itemNbt);
        return item;
    }

    private static void setPotionEffect(String id, int amplifier, int duration, ItemStack item) {
        removePotionEffect(id, item);
        NbtCompound nbt = item.getNbt();
        if(nbt == null) nbt = new NbtCompound();
        NbtList custom = nbt.getList("custom_potion_effects", 10);
        NbtCompound potion = new NbtCompound();
        potion.putString("id", "minecraft:" + id);
        potion.putByte("amplifier", (byte) amplifier);
        potion.putInt("duration", duration);
        custom.add(potion);
        nbt.put("custom_potion_effects", custom);
        item.setNbt(nbt);
    }

    private static boolean removePotionEffect(String id, ItemStack item) {
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return false;
        NbtList custom = nbt.getList("custom_potion_effects", 10);
        if(custom.isEmpty()) return false;
        int i = 0;
        while(true) {
            NbtCompound element = custom.getCompound(i);
            if(element.getString("id").equals("minecraft:" + id)) {
                custom.remove(i);
                break;
            }
            i++;
            if(i >= custom.size()) {
                return false;
            }
        }
        nbt.put("custom_potion_effects", custom);
        item.setNbt(nbt);
        return true;
    }

    private static HashMap<String, PotionData> getPotionEffects(ItemStack item) {
        HashMap<String, PotionData> result = new HashMap<>();
        NbtCompound nbt = item.getNbt();
        if(nbt == null) return result;
        NbtList custom = nbt.getList("custom_potion_effects", 10);
        if(custom.isEmpty()) return result;
        for(NbtElement el : custom) {
            if(!(el instanceof NbtCompound potion)) continue;
            String id = potion.getString("id").replace("minecraft:", "");
            byte amplifier = potion.getByte("amplifier");
            int duration = potion.getInt("duration");
            result.put(id, new PotionData(duration, amplifier));
        }
        return result;
    }

    private record PotionData(int duration, byte amplifier) {}
}
