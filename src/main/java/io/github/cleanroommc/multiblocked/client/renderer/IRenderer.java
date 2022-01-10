package io.github.cleanroommc.multiblocked.client.renderer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface IRenderer {

    @SideOnly(Side.CLIENT)
    void register();

    @SideOnly(Side.CLIENT)
    List<BakedQuad> renderSide(EnumFacing facing, BlockRenderLayer layer);

    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getParticleTexture();
}
