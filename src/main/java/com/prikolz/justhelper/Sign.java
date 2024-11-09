package com.prikolz.justhelper;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sign {
    public final int x;
    public final int y;
    public final int z;

    public Sign( int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "Sign(" + this.x + " " + this.y + " " + this.z + ")";
    }

    public static final HashMap<String, Sign> signs = new HashMap<>();
    public static List<SignInfo> history = null;

    public static void clear() { signs.clear(); }
    public static void add(BlockPos pos) {
        Sign sign = new Sign(pos.getX(), pos.getY(), pos.getZ());
        Sign.signs.put( pos.getX() + " " + pos.getY() + " " + pos.getZ(), sign );
    }

    public static void searchSigns(FabricClientCommandSource source, String search, boolean printAll) {
        if(Config.signGenerateMethod == null && Config.useCustomOutputClass) {
            source.sendFeedback(
                    Text.literal("♯ Не найден класс сообщений! Пожалуйста, выполните /justhelper reload_config")
                            .setStyle(Style.EMPTY
                                    .withColor(Formatting.YELLOW))
            );
            return;
        }
        ClientWorld world = source.getWorld();
        boolean founded;
        SignBlockEntity ent;
        List<SignInfo> data = new ArrayList<>();
        for(Sign sign : Sign.signs.values()) {
            try {
                founded = printAll;
                ent = (SignBlockEntity) world.getBlockEntity(new BlockPos(sign.x, sign.y, sign.z));
                if(ent == null) continue;
                for(Text text : ent.getFrontText().getMessages(false)) {
                    if(text.getString().toLowerCase().contains(search.toLowerCase())) founded = true;
                }
                if(!founded) continue;
                data.add( new SignInfo(sign.x, sign.y, sign.z, ent.getFrontText().getMessages(false)) );
            }catch (Exception ignore ) {}
        }
        if (data.isEmpty()) {
            source.sendFeedback(
                    Text.literal("♯ Не найдены таблички с \"" + search + "\"")
                            .setStyle(Style.EMPTY
                                    .withColor(Formatting.DARK_GRAY))
            );
            return;
        }
        if (printAll) {
            source.sendFeedback(Text.literal("\n♯ Содержания табличек:"));
        }else {
            source.sendFeedback(Text.literal("\n♯ Таблички с содержанием \"" + search + "\":"));
        }
        for(SignInfo info : data) {
            source.sendFeedback( info.generate() );
        }
        history = data;
        source.sendFeedback(Text.literal(""));
    }

    public static SignInfo getInfo(Sign sign) {
        var ent = (SignBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(new BlockPos(sign.x, sign.y, sign.z));
        if(ent == null) return null;
        return new SignInfo(sign.x, sign.y, sign.z, ent.getFrontText().getMessages(false));
    }
}
