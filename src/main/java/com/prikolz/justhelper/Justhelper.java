package com.prikolz.justhelper;

import com.prikolz.justhelper.commands.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Justhelper implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("justhelper");

	@Override
	public void onInitialize() {

		JusthelperCommand.register();

        try {
            Config.initialize();
			for(String msg : Config.messages) {
				LOGGER.warn( msg );
			}
        } catch (Exception e) {
            LOGGER.error("Error config: " + e.getMessage());
			e.printStackTrace();
        }
	}

}