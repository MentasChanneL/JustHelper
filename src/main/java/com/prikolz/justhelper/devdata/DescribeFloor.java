package com.prikolz.justhelper.devdata;

import com.prikolz.justhelper.util.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class DescribeFloor {

    public static final HashMap<Integer, Integer> ents = new HashMap<>();

    public static void describeDevWorld() {
        if(!ClientUtils.InDev()) return;
        DevData data = DevData.data.get(ClientUtils.worldName());
        if(data == null) return;
        for(int d : data.describes.keySet()) {
            String describe = data.describes.get(d);
            createInWorld(describe, d);
        }
    }

    public static void addDescribe(int floor, String text) {
        if(!ClientUtils.InDev()) return;
        DevData data = DevData.data.get(ClientUtils.worldName());
        if(data == null) {
            data = new DevData(ClientUtils.worldName(), new HashMap<>());
            DevData.data.put(ClientUtils.worldName(), data);
        }
        if(ents.containsKey(floor)) {
            ClientUtils.getWorld().removeEntity(ents.get(floor), Entity.RemovalReason.KILLED);
            ents.remove(floor);
        }
        if(text.isEmpty()) {
            data.describes.remove(floor);
            try{DevData.Write();}catch(Exception e) { ClientUtils.send("Ошибка записи dev_data.json! " + e.getMessage()); };
            return;
        }
        data.describes.put(floor, text);
        try{DevData.Write();}catch(Exception e) { ClientUtils.send("Ошибка записи dev_data.json! " + e.getMessage()); };
        createInWorld(text, floor);
    }

    public static void createInWorld(String desc, int floor) {
        if(!ClientUtils.InDev()) return;
        try {
            desc = desc.replaceAll("&", "§").replaceAll("%ss", "     ").replaceAll("%s", " ");
            NbtCompound main = NbtHelper.fromNbtProviderString("{id:\"minecraft:text_display\",Rotation:[90F,0F],transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,-1.5f,0f],scale:[10f,10f,10f]},text:'\"" + desc +"\"',background:" + -11382190 + "}");
            Entity ent = EntityType.getEntityFromNbt(main, ClientUtils.getWorld(), SpawnReason.NATURAL).get();
            BlockPos pos = new BlockPos(-1, 4 + (7 * (floor - 1)), 47);
            ent.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            ClientUtils.getWorld().addEntity(ent);
            ents.put(floor, ent.getId());
        }catch (Exception e) {
            ClientUtils.send("Ошибка создания text_display! " + e.getMessage());
        }
    }
}
