package com.prikolz.justhelper;

import com.prikolz.justhelper.util.ClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ClickMessage {
    public static void click(int line, Text message, int mouse) {
        String run =  Config.clickMessageConfig.clickRight;
        if(mouse == 2) {
            run = Config.clickMessageConfig.clickMiddle;
        }
        if(!run.startsWith("<")) return;
        run = run
                .replaceAll("<message>", message.getString())
                .replaceAll("<line>", String.valueOf(line))
        ;
        int index = run.indexOf(">");
        if (index == -1) return;
        String action = run.substring(1, index);
        run = run.substring(index + 1);
        Action a = Action.valueOf(action.toUpperCase());
        a.run(line, message, mouse, run);
    }

    private enum Action {

        RUN(new ActionRun() {
            @Override
            public void run(int line, Text message, int mouse, String run) {
                CommandBuffer.sendCommand(run);
            }
        }),
        COPY(new ActionRun() {
            @Override
            public void run(int line, Text message, int mouse, String run) {
                MinecraftClient.getInstance().keyboard.setClipboard(run);
            }
        }),
        SAY(new ActionRun() {
            @Override
            public void run(int line, Text message, int mouse, String run) {
                ClientUtils.getPlayer().networkHandler.sendChatMessage(run);
            }
        })
        ;

        private final ActionRun run;

        Action(ActionRun run) {
            this.run = run;
        }

        public void run(int line, Text message, int mouse, String run) {
            this.run.run(line, message, mouse, run);
        }
    }

    private interface ActionRun {
        void run(int line, Text message, int mouse, String run);
    }
}
