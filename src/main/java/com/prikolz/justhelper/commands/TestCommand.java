package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.MyScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;


public class TestCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal("jhtest")
                        .executes(context -> {
                            MinecraftClient.getInstance().setScreenAndRender(new MyScreen());
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
