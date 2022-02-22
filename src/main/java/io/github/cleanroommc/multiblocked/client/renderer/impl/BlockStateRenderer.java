package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.util.FacadeBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStateRenderer implements IRenderer {

    public final IBlockState state;
    @SideOnly(Side.CLIENT)
    private transient IBakedModel itemModel;

    public BlockStateRenderer(IBlockState state) {
        this.state = state;
    }

    protected IBlockState getState() {
        return state;
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

    @Override
    public TextureAtlasSprite getParticleTexture() {
        IBlockState state = getState();
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        return getItemModel(renderItem).getParticleTexture();
    }
}
