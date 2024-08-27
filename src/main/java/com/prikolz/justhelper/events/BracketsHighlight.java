package com.prikolz.justhelper.events;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
import org.lwjgl.opengl.GL11;

public class BracketsHighlight {

    public static void register() {
        UseBlockCallback.EVENT.register(BracketsHighlight::onBlockRightClick);

        WorldRenderEvents.LAST.register(BracketsHighlight::renderOutline);
    }

    private static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos clickedBlockPos = hitResult.getBlockPos();
        BlockState clickedBlockState = world.getBlockState(clickedBlockPos);
        Block clickedBlock = clickedBlockState.getBlock();

        if (clickedBlock == Blocks.PISTON && world.isClient) {
            POSITIONS_TO_RENDER.clear();
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

                        POSITIONS_TO_RENDER.add(clickedBlockPos);
                        POSITIONS_TO_RENDER.add(currentPos);
                        break;
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    private static final Set<BlockPos> POSITIONS_TO_RENDER = new HashSet<>();

    public static void renderOutline(WorldRenderContext context) {
        ClientWorld world = context.world();

        for (BlockPos pos : POSITIONS_TO_RENDER) {
            BlockState state = world.getBlockState(pos);

            Color color = new Color(255, 255, 0, 255);
            float[] colorComponents = new float[] {color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F};

            state.getOutlineShape(world, pos).forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double zFightingOffset = 0.001;
                Box box = new Box(
                    (float) (pos.getX() + minX - zFightingOffset),
                    (float) (pos.getY() + minY - zFightingOffset),
                    (float) (pos.getZ() + minZ - zFightingOffset),
                    (float) (pos.getX() + maxX + zFightingOffset),
                    (float) (pos.getY() + maxY + zFightingOffset),
                    (float) (pos.getZ() + maxZ + zFightingOffset)
                );

                renderOutlineHelper(context, box, colorComponents, 2.5F);
            });
        }
    }

    public static void renderOutlineHelper(WorldRenderContext context, Box box, float[] colorComponents, float lineWidth) {
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        matrices.push();
        matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        WorldRenderer.drawBox(matrices, buffer, box, colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
        tessellator.draw();

        matrices.pop();
        RenderSystem.lineWidth(1F);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }
}
