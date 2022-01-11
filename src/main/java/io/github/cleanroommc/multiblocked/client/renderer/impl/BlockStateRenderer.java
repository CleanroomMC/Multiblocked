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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStateRenderer implements IRenderer {
    private static BlockRendererDispatcher BRD;
    private static RenderItem RI;

    public final IBlockState state;
    @SideOnly(Side.CLIENT)
    private IBakedModel itemModel;

    public BlockStateRenderer(IBlockState state) {
        this.state = state;
    }
    
    @Override
    public void renderItem(ItemStack stack) {
        if (RI == null) RI = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        if (itemModel == null) {
            itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
        }
        RI.renderItem(renderItem, itemModel);
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        if (BRD == null) BRD  = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, null, this.state);
        BRD.renderBlockDamage(this.state, pos, texture, access);
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        if (BRD == null) BRD  = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, null, this.state);
        BRD.renderBlock(this.state, pos, access, buffer);
        return false;
    }

}
