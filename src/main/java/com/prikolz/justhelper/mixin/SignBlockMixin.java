package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Sign;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public class SignBlockMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectInit(BlockPos pos, BlockState state, CallbackInfo ci) {
        Sign.add(pos);
    }
}
