package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class ParticleRenderer implements IRenderer {

    protected boolean isBackLayer = true;
    protected boolean isAddBlend;

    @SideOnly(Side.CLIENT)
    public transient AbstractParticle particle;

    protected abstract AbstractParticle createParticle(World world, double x, double y, double z);

    @Override
    public void renderItem(ItemStack stack) {
        
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @Override
    public final void onPreAccess(TileEntity te) {
        if (te instanceof ComponentTileEntity<?>) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) te;
            BlockPos pos = te.getPos();
            AbstractParticle particle = createParticle(te.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            component.rendererObject = particle;
            if (particle != null) {
                particle.setOnUpdate(p -> {
                    if (component.isInvalid() || component.getWorld().getTileEntity(component.getPos()) != component) p.kill();
                });
                particle.addParticle();
            }
        }
    }

    @Override
    public final void onPostAccess(TileEntity te) {
        if (te instanceof ComponentTileEntity<?>) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) te;
            if (component.rendererObject instanceof AbstractParticle) {
                ((AbstractParticle) component.rendererObject).kill();
                component.rendererObject = null;
            }
        }
    }

    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        if (world instanceof DummyWorld) {
            if (particle == null) {
                particle = createParticle(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (particle == null) return false;
            }
            return particle.isBackLayer() && pass == 0 || !particle.isBackLayer() && pass == 1;
        } return false;
    }

    @Override
    public boolean hasTESR() {
        return true;
    }

    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // used for Scene widget
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        RenderHelper.disableStandardItemLighting();

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        particle.setWorld(te.getWorld());
        particle.setPosition(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5);

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (particle.isAddBlend()) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }

        particle.getGLHandler().preDraw(builder);
        particle.renderParticle(builder, partialTicks);
        particle.getGLHandler().postDraw(builder);

        if (particle.isAddBlend()) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        RenderHelper.enableStandardItemLighting();
    }

}
