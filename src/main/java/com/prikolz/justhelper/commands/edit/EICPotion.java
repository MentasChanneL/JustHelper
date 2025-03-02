package com.prikolz.justhelper.commands.edit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.commands.argumens.ColorArgumentType;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EICPotion {

    private static final HashMap<String, RegistryEntry<StatusEffect>> potionList = initPotion();

    private static HashMap<String, RegistryEntry<StatusEffect>> initPotion() {
        var result = new HashMap<String, RegistryEntry<StatusEffect>>();

        for(RegistryKey<StatusEffect> key : Registries.STATUS_EFFECT.getKeys()) {
            result.put( key.getValue().getPath(), Registries.STATUS_EFFECT.getEntry(Registries.STATUS_EFFECT.get(key)) );
        }

        return result;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("potion")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList.keySet()))
                                .then(ClientCommandManager.argument("amplifier", IntegerArgumentType.integer(0, 256))
                                                .then(ClientCommandManager.argument("duration", IntegerArgumentType.integer(0))
                                                        .then(ClientCommandManager.argument("show_particles", new VariantsArgumentType("Ожидалось true или false", false, "true", "false") )
                                                                .then(ClientCommandManager.argument("show_icon", new VariantsArgumentType("Ожидалось true или false", false, "true", "false") )
                                                                        .executes(context -> {
                                                                            if( EditItemCommand.msgItemIsNull(context) ) return 0;
                                                                            ItemStack item = EditItemCommand.getItemMainHand();
                                                                            String id = VariantsArgumentType.getParameter(context, "id");
                                                                            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
                                                                            int duration = IntegerArgumentType.getInteger(context, "duration");
                                                                            boolean showParticle = VariantsArgumentType.getParameter(context, "show_particles").equals("true");
                                                                            boolean showIcon = VariantsArgumentType.getParameter(context, "show_icon").equals("true");
                                                                            addPotionEffect(item, id, amplifier, duration, showParticle, showIcon);
                                                                            EditItemCommand.setItemMainHand(item);
                                                                            context.getSource().sendFeedback(
                                                                                    Text.literal("Предмету установлен эффект зелья ").setStyle(JustCommand.white)
                                                                                            .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.success))
                                                                                            .append(Text.literal(" силой "))
                                                                                            .append(Text.literal("" + amplifier).setStyle(JustCommand.success))
                                                                                            .append(Text.literal(" длительностью в "))
                                                                                            .append(Text.literal("" + duration).setStyle(JustCommand.success))
                                                                            );
                                                                            return 1;
                                                                        })
                                                                )
                                                        )
                                                )
                                )
                ))
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList.keySet()))
                                .executes(context -> {
                                    if( EditItemCommand.msgItemIsNull(context) ) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    String id = VariantsArgumentType.getParameter(context, "id");
                                    boolean deleted = removePotionEffect(item, id);
                                    if(deleted) {
                                        EditItemCommand.setItemMainHand(item);
                                        context.getSource().sendFeedback(
                                                Text.literal("Удален эффект зелья ").setStyle(JustCommand.white)
                                                        .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.success))
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
                        .then(ClientCommandManager.argument("id", new VariantsArgumentType("argument.id.unknown", true, potionList.keySet()))
                                .executes(context -> {
                                    if( EditItemCommand.msgItemIsNull(context) ) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    String id = VariantsArgumentType.getParameter(context, "id");
                                    var effects = getPotionEffects(item);
                                    if(!effects.containsKey(id)) {
                                        context.getSource().sendFeedback(
                                                Text.literal("Эффект ").setStyle(JustCommand.warn)
                                                        .append(Text.translatable("effect.minecraft." + id).setStyle(JustCommand.white))
                                                        .append(Text.literal(" не найден"))
                                        );
                                        return 0;
                                    }
                                    context.getSource().sendFeedback( displayEffect(effects.get(id)) );
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("color")
                        .then(ClientCommandManager.argument("color", new ColorArgumentType())
                                .executes(context -> {
                                    if( EditItemCommand.msgItemIsNull(context) ) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    int color = ColorArgumentType.getParameter(context, "color");
                                    String sColor = StringArgumentType.getString(context, "color");
                                    colorPotion(item, color);
                                    EditItemCommand.setItemMainHand(item);
                                    context.getSource().sendFeedback(
                                            Text.literal("Установлен ").setStyle(JustCommand.white)
                                                    .append(Text.literal("цвет").setStyle(Style.EMPTY
                                                            .withColor(color)
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("#" + sColor).withColor(color)))
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "#" + sColor))
                                                    ))
                                                    .append(Text.literal(" зелья").setStyle(JustCommand.white))
                                    );
                                    return 1;
                                })
                        )
                )
                .executes(context -> {
                    if( EditItemCommand.msgItemIsNull(context) ) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    context.getSource().sendFeedback(
                            Text.literal("\nУстановленные эффекты:\n⏷").setStyle(JustCommand.white)
                    );
                    var effects = getPotionEffects(item);
                    for(String key : effects.keySet()) {
                        context.getSource().sendFeedback( displayEffect(effects.get(key)) );
                    }
                    context.getSource().sendFeedback(Text.literal("⏶"));
                    return 1;
                });
        return result;
    }

    private static void addPotionEffect(ItemStack item, String id, int amplifier, int duration, boolean showParticle, boolean showIcon) {
        removePotionEffect(item, id);
        PotionContentsComponent component = item.get(DataComponentTypes.POTION_CONTENTS);
        List<StatusEffectInstance> list = new LinkedList<>();
        int color = 0;
        var potion = Optional.of(Registries.POTION.getEntry(Registries.POTION.get(0)));
        if(component != null) {
            list.addAll(component.customEffects());
            color = component.getColor();
            potion = component.potion();
        }
        list.add(new StatusEffectInstance(potionList.get(id), duration, amplifier, false, showParticle, showIcon));
        item.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion, Optional.of(color), list));
    }

    private static boolean removePotionEffect(ItemStack item, String id) {
        PotionContentsComponent component = item.get(DataComponentTypes.POTION_CONTENTS);
        if(component == null || component.customEffects().isEmpty() ) return false;
        List<StatusEffectInstance> list = new LinkedList<>(component.customEffects());
        AtomicBoolean result = new AtomicBoolean(false);
        list.removeIf(entry -> {
            if(entry.getEffectType().matchesId( Identifier.of(id) )) { result.set(true); return true; }
            return false;
        });
        if(result.get()) item.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(component.potion(), Optional.of(component.getColor()), list));
        return result.get();
    }

    private static void colorPotion(ItemStack item, int color) {
        PotionContentsComponent component = item.get(DataComponentTypes.POTION_CONTENTS);
        if(component == null) {
            component = new PotionContentsComponent(Optional.of(Registries.POTION.getEntry(Registries.POTION.get(0))), Optional.of(color), List.of());
            item.set(DataComponentTypes.POTION_CONTENTS, component);
            return;
        }
        component = new PotionContentsComponent(component.potion(), Optional.of(color), component.customEffects());
        item.set(DataComponentTypes.POTION_CONTENTS, component);
    }

    private static HashMap<String, StatusEffectInstance> getPotionEffects(ItemStack item) {
        var result = new HashMap<String, StatusEffectInstance>();
        PotionContentsComponent component = item.get(DataComponentTypes.POTION_CONTENTS);
        if(component == null || component.customEffects().isEmpty() ) return result;
        for(StatusEffectInstance effect : component.customEffects()) {
                result.put( Registries.STATUS_EFFECT.getKey(effect.getEffectType().value()).get().getValue().getPath(), effect );
            }
        return result;
    }

    private static Text displayEffect(StatusEffectInstance effect) {
        String id =  Registries.STATUS_EFFECT.getKey(effect.getEffectType().value()).get().getValue().getPath();
        String name = effect.getTranslationKey();
        int duration = effect.getDuration();
        int amplifier = effect.getAmplifier();
        String showParticle = effect.shouldShowParticles() ? "Да" : "Нет";
        String showIcon = effect.shouldShowIcon() ? "Да" : "Нет";
        return Text.literal(" • ")
                .append(Text.translatable(name).setStyle(JustCommand.warn
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать название\n" + id)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id))
                ))
                .append(Text.literal(" | ").setStyle(JustCommand.white))
                .append(Text.literal("Сила: ").setStyle(JustCommand.aqua))
                .append(Text.literal(amplifier + " ").setStyle(JustCommand.white))
                .append(Text.literal("Время: ").setStyle(JustCommand.success))
                .append(Text.literal(duration + " | ").setStyle(JustCommand.white))
                .append(Text.literal("Частицы: ").setStyle(JustCommand.gold))
                .append(Text.literal(showParticle + " ").setStyle(JustCommand.white))
                .append(Text.literal("Иконка: ").setStyle(JustCommand.gold))
                .append(Text.literal(showIcon).setStyle(JustCommand.white))
                ;
    }

}
