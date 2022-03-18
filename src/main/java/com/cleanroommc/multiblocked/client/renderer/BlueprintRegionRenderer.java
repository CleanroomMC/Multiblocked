package com.cleanroommc.multiblocked.client.renderer;

import com.cleanroommc.multiblocked.api.item.ItemBlueprint;
import com.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlueprintRegionRenderer {

    public static void render(RenderWorldLastEvent event) {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        ItemStack held = p.getHeldItemMainhand();
        if (held.getItem() instanceof ItemBlueprint) {
            BlockPos[] poses = ItemBlueprint.getPos(held);
            if (poses == null) return;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.getPartialTicks();
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.getPartialTicks();
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            RenderBufferUtils.renderCubeFace(buffer, poses[0].getX(), poses[0].getY(), poses[0].getZ(), poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1, 0.2f, 0.2f, 1f, 0.25f, true);

            tessellator.draw();


            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            GlStateManager.glLineWidth(3);

            RenderBufferUtils.renderCubeFrame(buffer, poses[0].getX(), poses[0].getY(), poses[0].getZ(), poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1, 0.0f, 0.0f, 1f, 0.5f);

            tessellator.draw();

            GlStateManager.enableCull();

            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.color(1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

}
