package com.prikolz.justhelper;

import com.prikolz.justhelper.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Justhelper implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("justhelper");

	@Override
	public void onInitialize() {

		JusthelperCommand.register();
		SignsCommand.register();
		ClipboardCommand.register();
		EditItemCommand.register();
		TestCommand.register();

        try {
            Config.initialize();
			for(String msg : Config.messages) {
				LOGGER.warn( msg );
			}
        } catch (Exception e) {
            LOGGER.error("Error config: " + e.getMessage());
			e.printStackTrace();
        }

        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
			if(!(blockEntity instanceof SignBlockEntity)) return;
			BlockPos pos = blockEntity.getPos();
			Sign sign = new Sign(pos.getX(), pos.getY(), pos.getZ());
			Sign.signs.put( pos.getX() + " " + pos.getY() + " " + pos.getZ(), sign );
		});

		ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
			if(!(blockEntity instanceof SignBlockEntity)) return;
			BlockPos pos = blockEntity.getPos();
			Sign.signs.remove( pos.getX() + " " + pos.getY() + " " + pos.getZ() );
		});
	}

}