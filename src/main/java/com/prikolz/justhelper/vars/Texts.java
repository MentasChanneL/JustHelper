package com.prikolz.justhelper.vars;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.prikolz.justhelper.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public abstract class Texts {

    public static String run(boolean clip) {
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (!clipboard.isEmpty()) {
            try {
                //int countTextPlaces = countSubstrings(Config.textGiveCommand, "%text%");
                //int free = (250 - Config.textGiveCommand.replaceAll("%text%", "").length()) / countTextPlaces;
                //if(free < 1) return "> В шаблонной команде нет свободного места! Укоротите команду в config.json!";
                //List<String> els = splitText(clipboard.replaceAll("\n",""), free);
                //
                //int i = 1;
                //for(String el : els) {
                //    String format = el;
                //    if(format.endsWith(" ")) format = format.substring(0, format.length() - 1);
                //    if(format.startsWith(" ")) format = format.substring(1);
                //    format = format.replaceAll("§", "");
                //    format = format.replaceAll("[\\x00-\\x1F\\x7F§]", "");
                //    CommandBuffer.sendCommand( Config.textGiveCommand.replaceAll("%text%", format).replaceAll("%index%", i + "") );
                //    i++;
                //}

                if (clipboard.length() > 16000 && !clip) {
                    return "> Текст слишком большой! Максимальный размер - 16 000 символов. Используйте аргумент +clip, чтобы разделить текст на несколько книг.";
                }


                List<String> els;
                if (clip) {
                    els = splitText(clipboard.replaceAll("\n", ""), Config.textsClipLimit);
                } else {
                    els = List.of(clipboard);
                }

                int[] slots = new int[els.size()];
                ClientPlayerEntity player = MinecraftClient.getInstance().player;

                int i = 0;
                for (int slot = 0; slot < 36; slot++) {
                    if (player.getInventory().getStack(slot).isEmpty()) {
                        slots[i] = slot;
                        i++;
                        if (i >= slots.length) {
                            break;
                        }
                    }
                }

                if (i + 1 < els.size()) {
                    return "> Нет свободных слотов в инвентаре!";
                }

                i = 0;

                for (String el : els) {
                    el = el.replaceAll("[\\x00-\\x1F\\x7F§]", "");
                    String inTag = el.replaceAll("\"", "\\\\\"");
                    String inDisplay = el.replaceAll("\"", "\\\\\\\\\"").replaceAll("'", "\\\\'");

                    String nbt = "{display: {Name: '{\"italic\":false,\"text\":\"" + inDisplay +
                                 "\"}', Lore: ['{\"italic\":false,\"color\":\"#ABC4D6\",\"extra\":[\" \",{\"color\":\"yellow\",\"translate\":\"creative_plus.argument.text.parsing_type.legacy\"}],\"translate\":\"creative_plus.argument.text.parsing_type\"}', '{\"italic\":false,\"color\":\"gray\",\"translate\":\"creative_plus.argument.text.parsing_type.about.legacy\"}', '{\"italic\":false,\"color\":\"#ABC4D6\",\"translate\":\"creative_plus.argument.text.raw_view\"}', '{\"italic\":false,\"color\":\"white\",\"text\":\"" +
                                 inDisplay + "\"}']}, creative_plus: {value: {type: \"text\", text: \"" + inTag + "\", parsing: \"legacy\"}}}";
                    ItemStack book = createNBTItemStack(new ItemStack(Items.BOOK), 1, nbt);
                    player.getInventory().setStack(slots[i], book);
                    i++;
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return "> " + e.getMessage();
            }
        }
        return "> Не удалось получить текст из буфера обмена";
    }

    public static List<String> splitText(String text, int segmentLength) {
        List<String> segments = new ArrayList<>();

        int length = text.length();
        for (int start = 0; start < length; start += segmentLength) {
            int end = Math.min(length, start + segmentLength);
            segments.add(text.substring(start, end));
        }

        return segments;
    }

    public static int countSubstrings(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    public static ItemStack createNBTItemStack(ItemStack item, int count, String nbt) {
        item.setCount(count);

        NbtCompound nbtc;
        try {
            nbtc = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        item.setNbt(nbtc);

        return item;
    }
}