package com.prikolz.justhelper.vars;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.Config;
import net.minecraft.client.MinecraftClient;

public abstract class TextsCommand {
    public static String run() {
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (!clipboard.isEmpty()) {
            try {
                int countTextPlaces = countSubstrings(Config.textGiveCommand, "%text%");
                int free = (250 - Config.textGiveCommand.replaceAll("%text%", "").length()) / countTextPlaces;
                if(free < 1) return "> В шаблонной команде нет свободного места! Укоротите команду в config.json!";
                String clip = clipboard.replaceAll("\"", "\\\\\\\\\"");
                List<String> els = splitText(clip, free);

                int i = 1;
                for(String el : els) {
                    CommandBuffer.sendCommand( Config.textGiveCommand.replaceAll("%text%", el).replaceAll("%index%", i + "") );
                    i++;
                }

                return els.size() + "";
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
