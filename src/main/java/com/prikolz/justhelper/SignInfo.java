package com.prikolz.justhelper;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class SignInfo {
    public final int x;
    public final int y;
    public final int z;
    public final Text[] lines;

    public SignInfo(int x, int y, int z, Text[] lines) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lines = lines;
    }

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

    private String toMini(String str) {
        String result = str;
        for(String key : miniSymvols.keySet()) {
            result = result.replaceAll(key, miniSymvols.get(key));
        }
        return result;
    }

    public Text generate() {
        if(!Config.useCustomOutputClass) {
            return JustSignOutput.generate(x, y, z, lines);
        }
        try {
            return (Text) Config.signGenerateMethod.invoke(Config.signGenerateInstance, this.x, this.y, this.z, this.lines);
        }catch (Exception e) {
            return Text.literal("Message generate error! " + e.getMessage()).setStyle(Style.EMPTY.withColor(Formatting.RED));
        }
    }
}
