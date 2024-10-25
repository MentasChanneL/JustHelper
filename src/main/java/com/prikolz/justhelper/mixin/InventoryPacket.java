package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.vars.VarHistory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryS2CPacket.class)
public class InventoryPacket {

    @Inject( method = "<init>", at = @At("RETURN") )
    private void inject1(int syncId, int revision, DefaultedList<ItemStack> contents, ItemStack cursorStack, CallbackInfo ci) {
        for(ItemStack i : contents) {
            VarHistory.analyseItemStack(i);
        }
    }
}
