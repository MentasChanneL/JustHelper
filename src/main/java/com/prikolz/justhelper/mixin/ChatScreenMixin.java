package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.ClickMessage;
import com.prikolz.justhelper.util.ClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    @Inject( method = "mouseClicked", at = @At("RETURN"))
    private void inject1(double mouseX, double mouseY, int button, CallbackInfoReturnable ci) {
        if (button == 1 || button == 2) {
            try {
                MinecraftClient.getInstance().inGameHud.getChatHud().getTextStyleAt(mouseX, mouseY);
                if(ClientUtils.line > -1) {
                    ClickMessage.click( ClientUtils.line, ClientUtils.msgs.get(ClientUtils.line).content(), button );
                }
            }catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
