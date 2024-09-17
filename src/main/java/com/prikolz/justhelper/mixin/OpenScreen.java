package com.prikolz.justhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(OpenScreenS2CPacket.class)
public abstract class OpenScreen {

    private static final Set<ScreenHandlerType<? extends ScreenHandler>> typesWl = Set.of(
            ScreenHandlerType.GENERIC_9X1,
            ScreenHandlerType.GENERIC_3X3,
            ScreenHandlerType.GENERIC_9X2,
            ScreenHandlerType.GENERIC_9X3,
            ScreenHandlerType.GENERIC_9X4,
            ScreenHandlerType.GENERIC_9X5,
            ScreenHandlerType.GENERIC_9X6
                    );

    @Shadow @Final private ScreenHandlerType<?> screenHandlerId;

    @Inject( method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V", at = @At("RETURN"))
    private void inject2(ClientPlayPacketListener clientPlayPacketListener, CallbackInfo ci) {
        if(typesWl.contains(this.screenHandlerId)) {
            //System.out.println(" ALLOW WL");
            //Screen screen = MinecraftClient.getInstance().currentScreen;
        }
    }
}
