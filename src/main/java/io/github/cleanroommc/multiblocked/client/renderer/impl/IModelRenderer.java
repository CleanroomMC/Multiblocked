package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.client.model.ModelFactory;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static io.github.cleanroommc.multiblocked.client.ClientProxy.registerNeeds;

public class IModelRenderer implements IRenderer {

    public final ResourceLocation modelLocation;
    @SideOnly(Side.CLIENT)
    private IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    private IBakedModel blockModel;

    public IModelRenderer(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        if (Multiblocked.isClient()) {
            registerNeeds.add(this);
        }
    }

    @Override
    public void renderItem(ItemStack stack) {
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        if (itemModel == null) {
            itemModel = ModelFactory.getModel(modelLocation).bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        ri.renderItem(stack, itemModel);
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        if (blockModel == null) {
            blockModel = ModelFactory.getModel(modelLocation).bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.BLOCK,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        blockModelRenderer.renderModel(blockAccess, blockModel, state, pos, buffer, true);
        return false;
    }

    @Override
    public void register(TextureMap map) {
        IModel model = ModelFactory.getModel(modelLocation);
        for (ResourceLocation texture : model.getTextures()) {
            map.registerSprite(texture);
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if (blockModel == null) {
            blockModel = ModelFactory.getModel(modelLocation).bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.BLOCK,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        return blockModel.getParticleTexture();
    }
}
