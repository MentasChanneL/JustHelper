package com.prikolz.justhelper.vars;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.vars.text.VarText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public abstract class Texts {
    public static String run(boolean clip) {
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (!clipboard.isEmpty()) {
            try {

                if(clipboard.length() > 16000 && !clip) return "> Текст слишком большой! Максимальный размер - 16 000 символов. Используйте аргумент +clip, чтобы разделить текст на несколько книг.";

                List<String> els;
                if(clip) {
                    els = splitText(clipboard.replaceAll("\n",""), ((Double)(Config.getCommand("clipboard").getDoubleParameter("clip_limit", 5000.0))).intValue() );
                }else{
                    els = List.of(clipboard);
                }

                int[] slots = new int[els.size()];
                ClientPlayerEntity player = MinecraftClient.getInstance().player;

                int i = 0;
                for(int slot = 0; slot < 36; slot++) {
                    if(player.getInventory().getStack(slot).isEmpty()) {
                        slots[i] = slot;
                        i++;
                        if(i >= slots.length) break;
                    }
                }

                if(i + 1 < els.size()) return "> Нет свободных слотов в инвентаре!";

                i = 0;

                for(String el : els) {
                    el = el.replaceAll("[\\x00-\\x1F\\x7F§]", "");
                    ItemStack item = VarText.getTextExemplar(el, "plain");
                    player.getInventory().setStack(slots[i], item);
                    MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(i, item));
                    i++;
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return "> " + e.getMessage();
            }
        }
        return "> Неудалось получить текст из буфера обмена";
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
}
