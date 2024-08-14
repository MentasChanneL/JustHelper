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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(CommandExecutionC2SPacket.class)
public class TeleportConfirm {
	@Inject( method = "<init>(Ljava/lang/String;Ljava/time/Instant;JLnet/minecraft/network/message/ArgumentSignatureDataMap;Lnet/minecraft/network/message/LastSeenMessageList$Acknowledgment;)V", at = @At("RETURN"))
	private void inject2(String string, Instant timestamp, long salt, ArgumentSignatureDataMap argumentSignatures, LastSeenMessageList.Acknowledgment acknowledgment, CallbackInfo ci) {
		if(!Config.enableBackTeleport) return;
		ClientPlayerEntity camera = MinecraftClient.getInstance().player;

		if((string.startsWith("tp ") || string.startsWith("teleport ")) && camera != null) {
			BlockEntity ent = camera.clientWorld.getBlockEntity(camera.getBlockPos());
			Text text = Text.literal("");
			Style orange = Style.EMPTY.withColor(Formatting.GOLD);
			if(ent instanceof SignBlockEntity) {
				SignBlockEntity sbe = (SignBlockEntity) ent;
				text = Text.literal("(")
						.append(sbe.getFrontText().getMessages(false)[1])
						.append(Text.literal(") "))
						.setStyle(orange)
				;
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
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + xyz))
									.withColor(Formatting.AQUA)
									.withHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, Text
											.literal("Нажмите, для телепортации\n\n" + JustSignOutput.mathFloor(camera.getBlockY()) + " этаж " + xyz)
											.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
											)
									)
							)
			);
		}
	}
}