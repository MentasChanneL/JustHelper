package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Sign;
import com.prikolz.justhelper.devdata.DescribeFloor;
import com.prikolz.justhelper.util.Scheduler;
import com.prikolz.justhelper.vars.VarHistory;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "<init>" ,at = @At("RETURN"))
    private void injectInit(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionTypeEntry, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        Sign.clear();
        String name = registryRef.getValue().getPath();
        try {
            VarHistory.saveJson();
            Scheduler.run(1000, () -> {
                DescribeFloor.ents.clear();
                DescribeFloor.describeDevWorld();
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
