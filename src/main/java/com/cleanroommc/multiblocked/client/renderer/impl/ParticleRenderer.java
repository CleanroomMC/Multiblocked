package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import com.cleanroommc.multiblocked.client.particle.IParticle;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.util.world.DummyWorld;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class ParticleRenderer implements ICustomRenderer {

    public boolean isBackLayer = true;
    public boolean isAddBlend;
    public int renderRange = -1;


    @SideOnly(Side.CLIENT)
    protected abstract AbstractParticle createParticle(ComponentTileEntity<?> te, double x, double y, double z);

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
            AbstractParticle particle = createParticle(component, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            component.rendererObject = particle;
            if (particle != null) {
                particle.setOnUpdate(p -> {
                    if (component.isInvalid() || component.getWorld().getTileEntity(component.getPos()) != component) p.kill();
                });
                particle.setImmortal();
                particle.addParticle();
                particle.setAddBlend(isAddBlend);
                particle.setBackLayer(isBackLayer);
                if (renderRange > 0) {
                    particle.setRenderRange(renderRange);
                }
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
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ComponentTileEntity<?>) {
                if (((ComponentTileEntity<?>) tileEntity).rendererObject instanceof IParticle) {
                    IParticle particle = (IParticle) ((ComponentTileEntity<?>) tileEntity).rendererObject;
                    return particle.isBackLayer() && pass == 0 || !particle.isBackLayer() && pass == 1;
                }
            }
        } return false;
    }

    @Override
    public boolean hasTESR(World world, BlockPos pos) {
        return world instanceof DummyWorld;
    }

    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IParticle particle = null;
        if (te instanceof ComponentTileEntity<?>) {
            if (((ComponentTileEntity<?>) te).rendererObject instanceof IParticle) {
                particle = (IParticle) ((ComponentTileEntity<?>) te).rendererObject;
            }
        }

        if (particle == null) return;

        // used for Scene widget
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        RenderHelper.disableStandardItemLighting();

        BufferBuilder builder = Tessellator.getInstance().getBuffer();

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

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject.addProperty("isBackLayer", isBackLayer);
        jsonObject.addProperty("isAddBlend", isAddBlend);
        jsonObject.addProperty("renderRange", renderRange);
        return jsonObject;
    }
}
