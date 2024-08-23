package com.prikolz.justhelper;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.prikolz.justhelper.vars.TextsCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class Justhelper implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("justhelper");
	public static final Style errorStyle = Style.EMPTY.withColor(Formatting.RED);
	public static final Style sucsessStyle = Style.EMPTY.withColor(Formatting.GREEN);
	public static final Style warnStyle = Style.EMPTY.withColor(Formatting.YELLOW);

	@Override
	public void onInitialize() {

		registerCommands();

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

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("signs")
							.then(argument("text", StringArgumentType.greedyString())
									.executes(context -> {
										String search = StringArgumentType.getString(context, "text");
										Sign.searchSigns(context.getSource(), search, false);
										return 1;
									})
							)
							.executes(context -> {
								Sign.searchSigns(context.getSource(), "-", true);
								return 1;
							})
			);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("justhelper")
							.then( ClientCommandManager.literal("reload_config" )
									.executes(context -> {
										context.getSource().sendFeedback(Text.literal("JustHelper > Перезагрузка конфига..."));
										try {
											Config.initialize();
											if(Config.useCustomOutputClass) {
												if(Config.compileCustomOutputClass) Config.compileJava();
												Config.loadCustomOutClass();
											}
										}catch (Exception e) {
											context.getSource().sendFeedback(
													Text.literal("Ошибка: ")
															.append(Text.literal(e.getMessage()))
															.setStyle(errorStyle)
											);
											e.printStackTrace();
											return 0;
										}
										for(String msg : Config.messages) {
											context.getSource().sendFeedback( Text.literal(msg) );
										}
										context.getSource().sendFeedback(Text.literal("JustHelper > Перезагрузка конфига выполнена!")
												.setStyle(sucsessStyle));
										return 1;
									})
							)
							.then( ClientCommandManager.literal("save_default_config" )
									.executes(context -> {
										context.getSource().sendFeedback(Text.literal("JustHelper > Сохранение стандартного конфига..."));
										try { Config.saveDefaultConfig(); } catch (Exception e) {
											context.getSource().sendFeedback(Text.literal("JustHelper > Ошибка сохранения стандартного конфига! " + e.getMessage()).setStyle(errorStyle));
											return 0;
										}
										context.getSource().sendFeedback(Text.literal("JustHelper > Стандартный конфиг сохранен!").setStyle(sucsessStyle));
										return 1;
									})

							)
							.executes(context -> {
								context.getSource().sendFeedback(
										Text.literal("JustHelper > Аргументы:").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
												.append( Text.literal("\nreload_config - Перезагрузить конфиг").setStyle(Style.EMPTY
														.withColor(Formatting.GOLD)) )
								);
								return 1;
							})
			);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("texts")
							.then( ClientCommandManager.literal("stop" )
									.executes(context -> {
										boolean result = CommandBuffer.stopBuffer();
										if(result) {
											context.getSource().sendFeedback(Text.literal("JustHelper > Поток команд остановлен"));
											return 1;
										}
										context.getSource().sendFeedback(Text.literal("JustHelper > Поток команд не был активен").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
										return 0;
									})
							)
							.executes(context -> {
								context.getSource().sendFeedback(
										Text.literal("JustHelper > Получение текстовых значений...").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
								);
								String err = TextsCommand.run();
								if(err.startsWith("> ")) {
									context.getSource().sendFeedback(Text.literal("JustHelper " + err).setStyle(Style.EMPTY.withColor(Formatting.RED)));
									return 0;
								}
								context.getSource().sendFeedback(
										Text.literal("JustHelper > Текст разделен на " + err + " частей!").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
								);
								context.getSource().sendFeedback(
										Text.literal("JustHelper > Для остановки введите /texts stop").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
								);
								return 1;
							})
			);
		});

	}

}