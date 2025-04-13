package com.prikolz.justhelper.devdata;

import com.prikolz.justhelper.util.ClientUtils;
import com.prikolz.justhelper.util.DevWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class DevComment {

    public static void commentWorld() {
        if(!ClientUtils.InDev()) return;
        DevData data = DevData.get();
        for (DevComment c : data.comments) c.create();
    }

    public static void addComment(boolean write, String text, int floor, int line, int x, float scale) {
        if(!ClientUtils.InDev()) return;
        remove(false, floor, line, x);
        DevData data = DevData.get();
        DevComment com = new DevComment(text, floor, line, x, scale);
        data.comments.add(com);
        if (write) try{DevData.Write();}catch(Exception e) { ClientUtils.send("Ошибка записи dev_data.json! " + e.getMessage()); };
        com.create();
    }

    public static boolean remove(boolean write, int floor, int line, int x) {
        if(!ClientUtils.InDev()) return false;
        DevData data = DevData.get();
        AtomicBoolean deleted = new AtomicBoolean(false);
        data.comments.removeIf(com -> {
            if (com.floor == floor && com.x == x && com.line == line ) {
                com.remove();
                deleted.set(true);
                return true;
            }
            return false;
        });
        if (write) try{DevData.Write();}catch(Exception e) { ClientUtils.send("Ошибка записи dev_data.json! " + e.getMessage()); };
        return deleted.get();
    }

    public Integer entityID = null;
    public String comment;
    public int floor;
    public int line;
    public int x;
    public float scale;

    public DevComment(String comment, int floor, int line, int x, float scale) {
        this.comment = comment;
        this.floor = floor;
        this.line = line;
        this.x = x;
        this.scale = scale;
    }

    public int create() {
        if(!ClientUtils.InDev()) return -1;
        try {
            String tscale = this.scale + "f," + this.scale + "f," + this.scale + "f";
            String desc = this.comment.replaceAll("&", "§").replaceAll("%ss", "     ").replaceAll("%s", " ").replaceAll("\\\\n", "\n");
            NbtCompound main = NbtHelper.fromNbtProviderString("{id:\"minecraft:text_display\",view_range:0.06f,transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[" + tscale + "]},text:'\"" + desc +"\"',background:" + 0 + "}");
            Entity ent = EntityType.getEntityFromNbt(main, ClientUtils.getWorld()).get();
            ent.setPos(this.x + 0.5, DevWorld.mathY(this.floor) + 1, DevWorld.mathZ(this.line) + 1.005);
            ClientUtils.getWorld().addEntity(ent);
            this.entityID = ent.getId();
            return ent.getId();
        }catch (Exception e) {
            ClientUtils.send("Ошибка создания text_display! " + e.getMessage());
            return -1;
        }
    }

    public void remove() {
        if (this.entityID == null) return;
        ClientUtils.getWorld().removeEntity(this.entityID, Entity.RemovalReason.KILLED);
    }
}
