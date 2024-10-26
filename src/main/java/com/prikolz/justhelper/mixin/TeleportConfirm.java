package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustSignOutput;
import com.prikolz.justhelper.Sign;
import com.prikolz.justhelper.SignInfo;
import com.prikolz.justhelper.commands.JustCommand;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandExecutionC2SPacket.class)
public class TeleportConfirm {

	private static long tpTimestamp = 0;

	@Inject( method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
	private void inject2(String string, CallbackInfo ci) {
		if(!Config.enableBackTeleport) return;
		ClientPlayerEntity camera = MinecraftClient.getInstance().player;
		if((string.startsWith("tp ") || string.startsWith("teleport ")) && camera != null) {
			if(System.currentTimeMillis() - tpTimestamp < 50) { return; }
			tpTimestamp = System.currentTimeMillis();
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
			try{ printHistorySigns(string); }catch (Exception ignore) {}

		}
	}

	private void printHistorySigns(String command) {
		String[] args = command.split(" ");
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int z = Integer.parseInt(args[3]);
		SignInfo data = getSign(x, y, z);
		int index = Sign.history.indexOf(data);
		if(data == null) return;
		SignInfo prewSign = null;
		SignInfo nextSign = null;
		if(index - 1 > -1) prewSign = Sign.history.get(index - 1);
		if(index + 1 < Sign.history.size()) nextSign = Sign.history.get(index + 1);
		MutableText out = Text.empty();
		if(prewSign != null) {
			out.append(getSignState("◀ ", prewSign, "◀ Предыдущая табличка"));
		}else {
			out.append( getEmpty("◀ ") );
		}
		out.append(Text.of(data.lines[0]).copy().setStyle(JustCommand.aqua));
		if(nextSign != null) {
			out.append(getSignState(" ▶", nextSign, "Следующая табличка ▶"));
		}else {
			out.append( getEmpty(" ▶") );
		}
		MutableText header = Text.empty().append(
				Text.of("—— ").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
				.append(Text.of(index + 1 + "/" + Sign.history.size())
				.copy().setStyle(JustCommand.white))
				.append(Text.of(" ——").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
		;
		MinecraftClient.getInstance().player.sendMessage(header);
		MinecraftClient.getInstance().player.sendMessage(out);
		MinecraftClient.getInstance().player.sendMessage(Text.of("———————").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
	}

	private MutableText getEmpty(String s) {
		return Text.empty().append(Text.of(s)).setStyle(
				Style.EMPTY.withColor(Formatting.DARK_GRAY)
		);
	}

	private MutableText getSignState(String s, SignInfo info, String d) {
		MutableText hover = Text.empty().append(Text.of(d + "\n—"));
		for(Text line : info.lines) {
			hover.append(Text.of("\n")).append(line);
		}
		return Text.empty().append(Text.of(s)).setStyle(Style.EMPTY
				.withColor(Formatting.WHITE)
				.withHoverEvent(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						hover
				))
				.withClickEvent(new ClickEvent(
						ClickEvent.Action.RUN_COMMAND,
						"/tp " + info.x + " " + info.y + " " + info.z
				))
		);
	}

	private SignInfo getSign(int x, int y, int z) {
		int i = 0;
		for(SignInfo info : Sign.history) {
			if(info.x == x && info.y == y && info.z == z) return info;
			i++;
		}
		return null;
	}
}