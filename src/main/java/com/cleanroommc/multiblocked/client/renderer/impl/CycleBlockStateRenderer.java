package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.client.util.FacadeBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * It will toggles the rendered block each second, mainly for rendering of the Any Capability. {@link MultiblockCapability#getCandidates()}}
 *
 * Because you did not schedule the chunk compiling. So please don't use it in the world. Just for JEI or such dynamic rendering.
 */
public class CycleBlockStateRenderer extends BlockStateRenderer {
    public final IBlockState[] states;
    public final TileEntity[] tileEntities;
    public int index;
    public long lastTime;

    public CycleBlockStateRenderer(IBlockState[] states) {
        super(Blocks.AIR.getDefaultState());
        if (states.length == 0) states = new IBlockState[]{super.state};
        this.states = states;
        this.tileEntities = new TileEntity[states.length];
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected IBakedModel getItemModel(ItemStack renderItem) {
        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected IBlockState getState() {
        long time = System.currentTimeMillis();
        if (time - lastTime > 1000) {
            lastTime = time;
            index = Multiblocked.RNG.nextInt();
        }
        return states[Math.abs(index) % states.length];
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = getState();
        int pass = MinecraftForgeClient.getRenderPass();
        BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockAccess access = new FacadeBlockAccess(te.getWorld(), te.getPos(), null, state);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            if (pass == 0 && layer == BlockRenderLayer.TRANSLUCENT) continue;
            if (pass == 1 && layer != BlockRenderLayer.TRANSLUCENT) continue;
            if (state.getBlock().canRenderInLayer(state, layer)) {
                ForgeHooksClient.setRenderLayer(layer);
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                brd.renderBlock(state, te.getPos(), access, buffer);

                Tessellator.getInstance().draw();
                Tessellator.getInstance().getBuffer();
            }
        }

        GlStateManager.shadeModel(7425);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        super.renderTESR(te, x, y, z, partialTicks, destroyStage, alpha);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR() {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return true;
    }

    public TileEntity getTileEntity(World world, BlockPos pos) {
        try {
            int i = Math.abs(index) % states.length;
            IBlockState state = states[i];
            if (!state.getBlock().hasTileEntity(state)) return null;
            if (tileEntities[i] == null) {
                tileEntities[i] = state.getBlock().createTileEntity(world, state);
                if (tileEntities[i] != null) {
                    tileEntities[i].setPos(pos);
                    tileEntities[i].setWorld(world);
                }
            }
            return tileEntities[i];
        } catch (Throwable throwable) {
            return null;
        }
    }
}
