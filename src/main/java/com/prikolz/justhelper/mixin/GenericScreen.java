package com.prikolz.justhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class GenericScreen {
    private static final Identifier CTEXTURE = new Identifier("textures/map/map_background.png");

    @Inject( method = "render", at= @At("RETURN"))
    private void inject1(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        //MinecraftClient client = MinecraftClient.getInstance();
        //int width = client.getWindow().getWidth();
        //int height = client.getWindow().getHeight();
        //System.out.println(width + " " + height);
        //context.drawTexture(CTEXTURE, width / 6, (int)(height / 6), 0, 0, 64, 64);
    }
}
