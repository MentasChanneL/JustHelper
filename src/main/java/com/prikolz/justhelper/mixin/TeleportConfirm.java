package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustSignOutput;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(CommandExecutionC2SPacket.class)
public class TeleportConfirm {

	private static boolean ignoreTp = false;

	@Inject( method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
	private void inject2(String string, CallbackInfo ci) {
		if(!Config.enableBackTeleport) return;
		ClientPlayerEntity camera = MinecraftClient.getInstance().player;
		if((string.startsWith("tp ") || string.startsWith("teleport ")) && camera != null) {
			if(ignoreTp) { ignoreTp = false; return; }
			ignoreTp = true;
			BlockPos entPos = new BlockPos(4, (camera.getBlockY() - 5) / 7 * 7 + 5, camera.getBlockZ() / 4 * 4 + 1);
			BlockEntity ent = camera.clientWorld.getBlockEntity(entPos);
			Text text = Text.literal("");
			MutableText fullText = Text.literal("");
			Style orange = Style.EMPTY.withColor(Formatting.GOLD);
			if(ent instanceof SignBlockEntity) {
				SignBlockEntity sbe = (SignBlockEntity) ent;
				text = Text.literal("(")
						.append(sbe.getFrontText().getMessages(false)[1])
						.append(Text.literal(") "))
						.setStyle(orange)
				;
				for(Text t : sbe.getFrontText().getMessages(false)) {
					fullText = fullText.append(t).append("\n");
				}
				fullText.setStyle(Style.EMPTY.withColor(Formatting.GRAY));

			}

			String xyz = camera.getBlockX() + " " + camera.getBlockY() + " " + camera.getBlockZ();
			String floor = JustSignOutput.toMini("(" + JustSignOutput.mathFloor(camera.getBlockY()) + ")");
			Style yellow = Style.EMPTY.withColor(Formatting.YELLOW);
			camera.sendMessage(
					Text.literal("" )
							.append( Text.literal("JustHelper »").setStyle(yellow) )
							.append( Text.literal(" Вернутся на ") )
							.append(text)
							.append( Text.literal( xyz + floor).setStyle(yellow) )
							.append( Text.literal(" (нажмите)").setStyle(Style.EMPTY.withColor(Formatting.AQUA)) )
							.setStyle( Style.EMPTY
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + xyz))
									.withColor(Formatting.AQUA)
									.withHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
											Text.literal("")
													.append(fullText)
													.append(Text.literal("\n" + JustSignOutput.mathFloor(camera.getBlockY()) + " этаж " + camera.getBlockZ() / 4 + " линия " + xyz + "\nНажмите, для телепортации"))
											.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
											)
									)
							)
			);
		}
	}
}