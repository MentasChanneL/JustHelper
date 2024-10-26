package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.vars.VarHistory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Inject( method = "setStack", at = @At("RETURN") )
    private void inject1(int slot, ItemStack stack, CallbackInfo ci) {
        if(!stack.isEmpty()) VarHistory.analyseItemStack(stack);
    }

}
