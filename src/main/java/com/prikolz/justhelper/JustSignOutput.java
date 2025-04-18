package com.prikolz.justhelper;

import com.prikolz.justhelper.util.DevWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public abstract class JustSignOutput {

    private static final HashMap<String, String> miniSymvols = setFloorsym();

    private static HashMap<String, String> setFloorsym() {
        HashMap<String, String> result = new HashMap<>();

        result.put("0", "₀");
        result.put("1", "₁");
        result.put("2", "₂");
        result.put("3", "₃");
        result.put("4", "₄");
        result.put("5", "₅");
        result.put("6", "₆");
        result.put("7", "₇");
        result.put("8", "₈");
        result.put("9", "₉");
        result.put("-", "₋");
        result.put("\\(", "₍");
        result.put("\\)", "₎");
        return result;
    }

    public static String toMini(String str) {
        String result = str;
        for(String key : miniSymvols.keySet()) {
            result = result.replaceAll(key, miniSymvols.get(key));
        }
        return result;
    }

    public static Text generate(int x, int y, int z, Text[] lines) {
        int mathFloor = DevWorld.mathFloor(y);
        String floor = toMini("(" + mathFloor + ")");
        MutableText hoverText = Text.literal("");
        MutableText copyText = Text.literal(" ");
        int i = 1;
        for (Text line : lines) {
            hoverText.append(line.copy().setStyle(Style.EMPTY.withColor(Formatting.WHITE))).append(Text.literal("\n"));
            if(line.getString() != null && !line.getString().isEmpty()) {
                copyText.append(
                        Text.of(toMini("(" + i + ")")).copy()
                                .setStyle( Style.EMPTY.withHoverEvent(
                                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Скопировать: " + line.getString()))
                                                )
                                                .withClickEvent(
                                                        new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, line.getString()))
                                )
                );
            }
            i++;
        }
        String xyz = x + " " + y + " " + z;
        hoverText.append(
                Text.literal("¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯\n" + xyz)
                        .append( Text.literal(" | " + mathFloor + " этаж") )
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)) );

        MutableText text = Text
                .literal("")
                .append(Text.literal(floor).setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                .append(Text.literal(" (" + x + " " + y + " " + z + ")")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.AQUA)))
                .append(Text
                        .literal(" → ")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.YELLOW)))
                .append(lines[0].copy().setStyle(
                        Style.EMPTY.withColor(Formatting.WHITE)
                ))
                .setStyle( Style.EMPTY
                        .withClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/tp " + xyz))
                        .withHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, hoverText ))
                )
                .append(copyText)
        ;
        return text;
    }
}