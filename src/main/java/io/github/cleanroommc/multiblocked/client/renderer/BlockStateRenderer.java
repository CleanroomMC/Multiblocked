package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.util.FacadeBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class BlockStateRenderer implements IRenderer{
    public final IBlockState state;
    @SideOnly(Side.CLIENT)
    private IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    private IBakedModel blockModel;

    public BlockStateRenderer(IBlockState state) {
        this.state = state;
    }

    @Override
    public void register(TextureMap textureMap) {

    }

    @Override
    public List<BakedQuad> renderSide(ComponentTileEntity component, EnumFacing facing, BlockRenderLayer layer, long rand) {
        List<BakedQuad> quads;
        if (component == null || layer == null) {
            if (itemModel == null) {
                itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state)), null, null);
            }
            quads = itemModel.getQuads(null, facing, rand);
//            itemQuads.put(side, FacadeRenderer.applyItemTint(CCQuad.fromArray(quads), renderStack));
        } else {
            if (state.getBlock().canRenderInLayer(state, layer)) {
                if (blockModel == null) blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
                IBlockState state = this.state;
                IBlockAccess access = new FacadeBlockAccess(component.getWorld(), component.getPos(), facing, state);
                try {
                    state = state.getActualState(access, component.getPos());
                } catch (Exception ignored) { }
                quads = blockModel.getQuads(state, facing, rand);
            } else {
                quads = Collections.emptyList();
            }
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return (blockModel == null ? blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state) : blockModel).getParticleTexture();
    }

}
