package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.FacadeBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockStateRenderer implements IRenderer {
    private final IBlockState state;
    @SideOnly(Side.CLIENT)
    private transient IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    private transient TileEntity tileEntity;

    public BlockStateRenderer(IBlockState state) {
        this.state = state;
    }

    public IBlockState getState() {
        return state == null ? Blocks.STONE.getDefaultState() : state;
    }

    @SideOnly(Side.CLIENT)
    protected IBakedModel getItemModel(ItemStack renderItem) {
        if (itemModel == null) {
            itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
        }
        return itemModel;
    }

    @Override
    public void renderItem(ItemStack stack) {
        IBlockState state = getState();
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        ri.renderItem(renderItem, getItemModel(renderItem));
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        state = getState();
        if (state.getBlock().canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
            BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, null, state);
            brd.renderBlockDamage(state, pos, texture, access);
        }
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        state = getState();
        if (state.getBlock().canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
            BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, null, state);
            return brd.renderBlock(state, pos, access, buffer);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR(World world, BlockPos pos) {
        TileEntity tileEntity = getTileEntity(world, pos);
        if (tileEntity == null) {
            return false;
        }
        return TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass()) != null;
    }

    public TileEntity getTileEntity(World world, BlockPos pos) {
        try {
            if (tileEntity != null) {
                if (world != null && pos != null) {
                    tileEntity.setPos(pos);
                    tileEntity.setWorld(world);
                }
                return tileEntity;
            }
            if (!getState().getBlock().hasTileEntity(getState())) return null;
            tileEntity = getState().getBlock().createTileEntity(world, getState());
            if (tileEntity != null) {
                tileEntity.setPos(pos);
                tileEntity.setWorld(world);
            }
            return tileEntity;
        } catch (Throwable throwable) {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        TileEntity tileEntity = getTileEntity(world, pos);
        return tileEntity != null && tileEntity.shouldRenderInPass(pass);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());
        if (tileEntity == null) return false;
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
        if (tesr != null) {
            return tesr.isGlobalRenderer(tileEntity);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());
        if (tileEntity == null) return;
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
        if (tesr != null) {
            tesr.render(tileEntity, x, y, z, partialTicks, destroyStage, alpha);
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        IBlockState state = getState();
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        return getItemModel(renderItem).getParticleTexture();
    }
}
