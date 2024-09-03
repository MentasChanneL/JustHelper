package com.prikolz.justhelper.mixin;


import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityUpdateS2CPacket.class)
public class BlockEntityLoad {
    @Inject(method = "<init>(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntityType;Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void inject(BlockPos pos, BlockEntityType blockEntityType, NbtCompound nbt, CallbackInfo ci) {
        System.out.println(blockEntityType.toString());
    }
}
