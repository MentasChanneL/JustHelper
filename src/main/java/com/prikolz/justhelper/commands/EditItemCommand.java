package com.prikolz.justhelper.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;

public class EditItemCommand {
    private static Set<Character> tagWhitelist = initWL();
    private static HashMap<String, String> operationSym = initOperationSym();

    private final static Set<Character> initWL() {
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

    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal("edit")
                        .then( ClientCommandManager.literal("tag" )
                                .then( ClientCommandManager.argument("name", StringArgumentType.string())
                                        .then( ClientCommandManager.argument("value", StringArgumentType.greedyString() )
                                                .executes(context -> {
                                                    if( msgItemIsNull(context) ) return 0;
                                                    ItemStack item = getItemMainHand();
                                                    String keyArg = StringArgumentType.getString(context, "name");
                                                    for(char c : keyArg.toCharArray()) {
                                                        if(!tagWhitelist.contains(c)) {
                                                            context.getSource().sendFeedback(Text.literal("JustHelper > Название тега содержит недопустимые символы! Название может содержать только: маленькие латинские(английские) буквы, цифры, нижнее подчеркивание или тире.").setStyle(JustCommand.error));
                                                            return 0;
                                                        }
                                                    }
                                                    String key = "justcreativeplus:" + keyArg;
                                                    String value = StringArgumentType.getString(context, "value");
                                                    NbtCompound[] tags = getNbt("PublicBukkitValues", item);
                                                    NbtCompound tag;
                                                    if(tags == null) {
                                                        tag = new NbtCompound();
                                                    }else{
                                                        tag = tags[tags.length - 1];
                                                    }
                                                    tag.putString(key, value);
                                                    item.setSubNbt("PublicBukkitValues", tag);
                                                    setItemMainHand(item);
                                                    context.getSource().sendFeedback(
                                                            Text.literal("")
                                                                    .append(Text.literal("Предмету установлен тег ").setStyle(JustCommand.sucsess))
                                                                    .append(Text.literal(keyArg).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                                    .append(Text.literal(" со значением ").setStyle(JustCommand.sucsess))
                                                                    .append(Text.literal(value).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                    );
                                                    return 1;
                                                })
                                        )
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            String key = StringArgumentType.getString(context, "name");
                                            NbtCompound[] tags = getNbt("PublicBukkitValues", item);
                                            if(tags == null || tags[tags.length - 1] == null || tags[tags.length - 1].isEmpty() || !(tags[tags.length - 1].getKeys().contains("justcreativeplus:" + key))) {
                                                context.getSource().sendFeedback(Text.literal("JustHelper > Тег не найден! Если вы хотите установить тег, укажите значение.").setStyle(JustCommand.warn));
                                                return 0;
                                            }
                                            String tag = tags[tags.length - 1].getString("justcreativeplus:" + key);
                                            String value = tag;
                                            if(value.length() > 10) value = value.substring(0, 10) + "...";
                                            context.getSource().sendFeedback(
                                                    Text.literal( key ).setStyle(Style.EMPTY
                                                            .withColor(Formatting.YELLOW)
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key))
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать ключ\n" + key))))
                                                            .append(Text.literal(" = ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                                                            .append(Text.literal(value).setStyle(Style.EMPTY
                                                                    .withColor(Formatting.GOLD)
                                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tag))
                                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать значение\n" + tag)))))
                                            );
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    if( msgItemIsNull(context) ) return 0;
                                    ItemStack item = getItemMainHand();
                                    NbtCompound[] tags = getNbt("PublicBukkitValues", item);
                                    if(tags == null || tags[tags.length - 1] == null || tags[tags.length - 1].isEmpty()) {
                                        context.getSource().sendFeedback(Text.literal("JustHelper > Теги не найдены").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    NbtCompound tag = tags[tags.length - 1];
                                    Set<String> keys = tag.getKeys();
                                    context.getSource().sendFeedback(Text.literal(""));
                                    context.getSource().sendFeedback(Text.literal("Установленные теги предмета:"));
                                    context.getSource().sendFeedback(Text.literal("⏷"));
                                    for(String key : keys) {
                                        String cutKey = key.substring(17);
                                        String value = tag.getString(key);
                                        String cutValue = value;
                                        if(cutValue.length() > 10) cutValue = cutValue.substring(0, 10) + "...";
                                        context.getSource().sendFeedback(
                                                Text.literal(" • ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                        .append(cutKey).setStyle(Style.EMPTY
                                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать ключ\n" + cutKey)))
                                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cutKey))
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
                                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            if( msgItemIsNull(context) ) return 0;
                                            ItemStack item = getItemMainHand();
                                            String arg = StringArgumentType.getString(context, "name");
                                            Text name = Text.literal(arg.replaceAll("&", "§").replaceAll("%space%", " ").replaceAll("%empty%", ""));
                                            item.setCustomName(name);
                                            setItemMainHand(item);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Заданно имя предмета: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                                                            .append(name)
                                            );
                                            return 1;
                                        })
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
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > Аргументы команды edit:").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                                            .append( Text.literal("\ntag - Добавить/Получить кастомный тег предмета").setStyle(JustCommand.gold))
                                            .append( Text.literal("\nrename - Изменить имя предмета. Поддерживаются коды цветов & и плейсхолдеры %empty%, %space%").setStyle(JustCommand.gold))
                                            .append( Text.literal("\nmodel - Изменить/Получить модель предмета(CustomModelData)").setStyle(JustCommand.gold) )
                                            .append( Text.literal("\nattribute - Добавляет/Удаляет/Получает атрибуты предмета.").setStyle(JustCommand.gold) )
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
    private static NbtCompound[] getNbt(String path, ItemStack item) {
        String[] args = path.split("\\.");
        NbtCompound[] result = new NbtCompound[args.length];
        NbtCompound main = item.getNbt();
        if(main == null) return null;
        int i = 0;
        NbtCompound current = main;
        for(String arg : args) {
            current = current.getCompound(arg);
            if(current == null) return null;
            result[i] = current;
            i++;
        }
        return result;
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
}
