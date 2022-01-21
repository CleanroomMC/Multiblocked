package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.block.ItemComponent;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ComponentRenderer implements ICustomItemRenderer {
    public static ComponentRenderer INSTANCE = new ComponentRenderer();
    public static final EnumBlockRenderType COMPONENT_RENDER_TYPE;
    static {
        COMPONENT_RENDER_TYPE = EnumHelper.addEnum(EnumBlockRenderType.class, "component_renderer", new Class[0]);
    }

    @Override
    public void renderItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemComponent) {
            IRenderer renderer = ((ItemComponent) stack.getItem()).getDefinition().getRenderer();
            if (renderer == null) return;
            renderer.renderItem(stack);
        }
    }

    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (tileEntity instanceof ComponentTileEntity) {
            IRenderer renderer = ((ComponentTileEntity<?>) tileEntity).getRenderer();
            if (renderer == null) return;
            renderer.renderBlockDamage(state, pos, texture, blockAccess);
        }
    }

    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (tileEntity instanceof ComponentTileEntity) {
            IRenderer renderer = ((ComponentTileEntity<?>) tileEntity).getRenderer();
            if (renderer == null) return false;
            return renderer.renderBlock(state, pos, blockAccess, buffer);
        } else {
            if (state.getBlock() instanceof BlockComponent) { // random capability
                IRenderer renderer =  ((BlockComponent) state.getBlock()).definition.baseRenderer;
                if (renderer == null) return false;
                return renderer.renderBlock(state, pos, blockAccess, buffer);
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }
}
