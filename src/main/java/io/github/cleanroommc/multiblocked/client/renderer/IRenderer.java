package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public interface IRenderer {

    @SideOnly(Side.CLIENT)
    void register(TextureMap textureMap);

    /**
     * Render!!!!!!!!!!!
     * @param component null -> item render
     */
    @SideOnly(Side.CLIENT)
    List<BakedQuad> renderSide(@Nullable ComponentTileEntity component, EnumFacing facing, BlockRenderLayer layer, long rand);

    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getParticleTexture();
}
