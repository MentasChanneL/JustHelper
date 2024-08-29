package com.prikolz.justhelper.events;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BracketsHighlight {

    public static final Set<BlockPos> HIGHLIGHTED_BLOCKS = new HashSet<>();

    public static void register() {
        UseBlockCallback.EVENT.register(BracketsHighlight::onBlockRightClick);
        AttackBlockCallback.EVENT.register(BracketsHighlight::onBlockAttack);

        WorldRenderEvents.LAST.register(BracketsHighlight::renderOutline);
    }

    private static ActionResult onBlockAttack(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
        HIGHLIGHTED_BLOCKS.clear();
        return ActionResult.PASS;
    }

    private static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos clickedBlockPos = hitResult.getBlockPos();
        BlockState clickedBlockState = world.getBlockState(clickedBlockPos);
        Block clickedBlock = clickedBlockState.getBlock();

        if (clickedBlock == Blocks.PISTON && world.isClient) {
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.FAIL;
            }

            if (HIGHLIGHTED_BLOCKS.contains(clickedBlockPos)) {
                HIGHLIGHTED_BLOCKS.clear();
                return ActionResult.PASS;
            } else {
                HIGHLIGHTED_BLOCKS.clear();
            }

            if (player.isSneaking()) {
                return ActionResult.PASS;
            }

            Direction clickedPistonFacing = clickedBlockState.get(Properties.FACING);

            int stepX;
            Direction targetDirection;
            if (clickedPistonFacing == Direction.EAST) {
                stepX = 1;
                targetDirection = Direction.WEST;
            } else if (clickedPistonFacing == Direction.WEST) {
                stepX = -1;
                targetDirection = Direction.EAST;
            } else {
                return ActionResult.PASS;
            }

            BlockPos currentPos = clickedBlockPos;
            int pistonsToSkip = 0;

            for (int i = 0; i < 85; i++) {
                currentPos = currentPos.add(stepX, 0, 0);

                BlockState blockState = world.getBlockState(currentPos);

                if (blockState.getBlock() == Blocks.PISTON) {
                    Direction pistonFacing = blockState.get(Properties.FACING);
                    if (pistonFacing == clickedPistonFacing) {
                        pistonsToSkip += 1;
                    } else if (pistonFacing == targetDirection) {
                        if (pistonsToSkip > 0) {
                            pistonsToSkip -= 1;
                            continue;
                        }

                        HIGHLIGHTED_BLOCKS.add(clickedBlockPos);
                        HIGHLIGHTED_BLOCKS.add(currentPos);
                        break;
                    }
                }
            }
        } else {
            HIGHLIGHTED_BLOCKS.clear();
        }
        return ActionResult.PASS;
    }

    public static void renderOutline(WorldRenderContext context) {
        ClientWorld world = context.world();

        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();

        Color color = new Color(255, 255, 0, 127);
        float[] colorComponents = new float[] {color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f};

        double zFightingOffset = 0.001;

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(2.5f);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        matrices.push();
        matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (BlockPos pos : HIGHLIGHTED_BLOCKS) {
            BlockState state = world.getBlockState(pos);

            state.getOutlineShape(world, pos).forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                Box box = new Box(
                    (float) (pos.getX() + minX - zFightingOffset),
                    (float) (pos.getY() + minY - zFightingOffset),
                    (float) (pos.getZ() + minZ - zFightingOffset),
                    (float) (pos.getX() + maxX + zFightingOffset),
                    (float) (pos.getY() + maxY + zFightingOffset),
                    (float) (pos.getZ() + maxZ + zFightingOffset)
                );
                WorldRenderer.drawBox(matrices, buffer, box, colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
            });
        }

        tessellator.draw();

        matrices.pop();
        RenderSystem.lineWidth(1f);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}
