package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.client.model.ModelFactory;
import io.github.cleanroommc.multiblocked.client.model.emissivemodel.EmissiveBakedModel;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
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
    protected IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    protected IBakedModel blockModel;

    public IModelRenderer(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        if (Multiblocked.isClient()) {
            registerNeeds.add(this);
        }
    }

    protected IModel getModel() {
        return ModelFactory.getModel(modelLocation);
    }

    @Override
    public void renderItem(ItemStack stack) {
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        if (itemModel == null) {
            itemModel = getModel().bake(
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
            blockModel = new EmissiveBakedModel(getModel().bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.BLOCK,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString())));
        }
        blockModelRenderer.renderModel(blockAccess, blockModel, state, pos, buffer, true);
        return true;
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        if (blockModel != null) {
            BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
            IBakedModel bakedModel = net.minecraftforge.client.ForgeHooksClient.getDamageModel(blockModel, texture, state, blockAccess, pos);
            blockModelRenderer.renderModel(blockAccess, bakedModel, state, pos, Tessellator.getInstance().getBuffer(), true);
        }
    }

    @Override
    public void register(TextureMap map) {
        IModel model = getModel();
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
