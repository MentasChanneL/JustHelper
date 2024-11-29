package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.util.ClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow
    private int getMessageLineIndex(double chatLineX, double chatLineY) { return 0; }
    @Shadow
    private double toChatLineY(double y) { return 0; }
    @Shadow
    private double toChatLineX(double x) { return 0; }
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    private List<ChatHudLine> messages;

    @Inject( method = "getTextStyleAt", at = @At("RETURN"))
    private void inject1(double x, double y, CallbackInfoReturnable ci) {
        try {
            double d = this.toChatLineX(x);
            double e = this.toChatLineY(y);
            int i = this.getMessageLineIndex(d, e);
            ClientUtils.line = i;
            ClientUtils.msgs = messages;
        }catch (Throwable ignore) {}
    }
}
