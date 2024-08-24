package com.prikolz.justhelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CommandBuffer {

    public static List<String> buffer = new ArrayList<>();
    private static boolean bufferActive = false;
    private static Timer bufferTimer = new Timer();
    private static TimerTask bufferBody = null;

    public static void sendCommand(String command) {
        buffer.add(command);
        if(!bufferActive) {
            bufferActive = true;
            bufferTimer = new Timer();
            bufferBody = new TimerTask() {
                @Override
                public void run() {
                    try{
                        String command = buffer.get(0);
                        buffer.remove(0);
                        ClientPlayerEntity player = MinecraftClient.getInstance().player;
                        System.out.println(">" + command + "<");
                        player.networkHandler.sendChatCommand(command);
                    }catch (Exception e) {
                        buffer.clear();
                    }

                    if( buffer.isEmpty() ) {
                        bufferActive = false;
                        bufferTimer.cancel();
                    }
                }
            };
            bufferTimer.schedule(bufferBody, 0, Config.commandBufferCD);
        }
    }

    public static boolean stopBuffer() {
        if(bufferActive) {
            bufferTimer.cancel();
            bufferActive = false;
            return true;
        }
        return false;
    }
}
