package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Sign;
import com.prikolz.justhelper.commands.EditItemCommand;
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

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "<init>" ,at = @At("RETURN"))
    private void injectInit(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionTypeEntry, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        Sign.clear();
        //Timer timer = new Timer();
//
        //TimerTask task = new TimerTask() {
        //    @Override
        //    public void run() {
        //        EditItemCommand.initEnchant();
        //        timer.cancel();
//
        //    }
        //};
        //timer.schedule(task, 5000);
    }
}
