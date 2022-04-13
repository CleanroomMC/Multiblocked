package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
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
    public final BlockInfo[] blockInfos;
    public int index;
    public long lastTime;

    @Override
    public String getType() {
        return "cycleblockstate";
    }

    public CycleBlockStateRenderer(BlockInfo[] blockInfos) {
        super(Blocks.AIR.getDefaultState());
        if (blockInfos.length == 0) blockInfos = new BlockInfo[]{new BlockInfo(Blocks.AIR)};
        this.blockInfos = blockInfos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected IBakedModel getItemModel(ItemStack renderItem) {
        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
    }

    public BlockInfo getBlockInfo() {
        long time = System.currentTimeMillis();
        if (time - lastTime > 1000) {
            lastTime = time;
            index = Multiblocked.RNG.nextInt();
        }
        return blockInfos[Math.abs(index) % blockInfos.length];
    }

    @Override
    public IBlockState getState() {
        return getBlockInfo().getBlockState();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = getState();
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());

        int pass = MinecraftForgeClient.getRenderPass();
        BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();

        TrackedDummyWorld dummyWorld = new TrackedDummyWorld(te.getWorld());
        dummyWorld.setBlockStateHook((pos1, iBlockState) -> pos1.equals(te.getPos()) ? state : iBlockState);
        if (tileEntity != null) {
            dummyWorld.setTileEntityHook((pos1, tile) -> pos1.equals(te.getPos()) ? tileEntity : tile);
            tileEntity.setPos(te.getPos());
            tileEntity.setWorld(dummyWorld);
        }

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
                if (state.getBlock() instanceof BlockComponent) {
                    IRenderer renderer = ((BlockComponent) state.getBlock()).definition.baseRenderer;
                    if (renderer != null) {
                        renderer.renderBlock(state, te.getPos(), dummyWorld, buffer);
                    }
                } else {
                    brd.renderBlock(state, te.getPos(), dummyWorld, buffer);
                }

                Tessellator.getInstance().draw();
            }
        }

        GlStateManager.shadeModel(7425);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        if (tileEntity == null) return;
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
        if (tesr != null) {
            tesr.render(tileEntity, x, y, z, partialTicks, destroyStage, alpha);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR(World world, BlockPos pos) {
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

    @SideOnly(Side.CLIENT)
    public TileEntity getTileEntity(World world, BlockPos pos) {
        return getBlockInfo().getTileEntity();
    }
}
