package com.prikolz.justhelper.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public abstract class ClientUtils {

    public static List<ChatHudLine> msgs = null;
    public static int line = 0;

    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
    public static void send(Object ... messages) {
        for(Object o : messages) {
            getPlayer().sendMessage( Text.literal(String.valueOf(o)) );
        }
    }
}
