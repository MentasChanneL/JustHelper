package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.vars.VarHistory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public class SetItemPacket {

    @Inject( method = "<init>", at = @At("RETURN") )
    private void inject1(int syncId, int revision, int slot, ItemStack i, CallbackInfo ci) {
        VarHistory.analyseItemStack(i);
    }
}
