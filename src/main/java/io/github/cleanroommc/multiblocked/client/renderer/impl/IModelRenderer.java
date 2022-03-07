package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cleanroommc.multiblocked.client.ClientProxy.registerNeeds;

public class IModelRenderer implements IRenderer {

    protected static final Set<ResourceLocation> CACHE = new HashSet<>();

    public final ResourceLocation modelLocation;
    public final Set<BlockRenderLayer> renderLayer;
    @SideOnly(Side.CLIENT)
    protected transient IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    protected transient Map<EnumFacing, IBakedModel> blockModels;

    public IModelRenderer(ResourceLocation modelLocation, BlockRenderLayer... renderLayers) {
        this.modelLocation = modelLocation;
        this.renderLayer = Arrays.stream(renderLayers).collect(Collectors.toSet());
        if (Multiblocked.isClient()) {
            if (isRaw()) {
                registerNeeds.add(this);
                CACHE.add(modelLocation);
            }
            blockModels = new EnumMap<>(EnumFacing.class);
        }
    }

    public IRenderer fromJson() {
        return new IModelRenderer(modelLocation, renderLayer.toArray(new BlockRenderLayer[0]));
    }

    protected IModel getModel() {
        return ModelFactory.getModel(modelLocation);
    }

    @Override
    public void renderItem(ItemStack stack) {
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        ri.renderItem(stack, getItemBakedModel());
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        if (!renderLayer.isEmpty() && !renderLayer.contains(MinecraftForgeClient.getRenderLayer())) return false;
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        blockModelRenderer.renderModel(blockAccess, getBlockBakedModel(pos, blockAccess), state, pos, buffer, true);
        return true;
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        IBakedModel bakedModel = net.minecraftforge.client.ForgeHooksClient.getDamageModel(getBlockBakedModel(pos, blockAccess), texture, state, blockAccess, pos);
        blockModelRenderer.renderModel(blockAccess, bakedModel, state, pos, Tessellator.getInstance().getBuffer(), true);
    }

    protected IBakedModel getItemBakedModel() {
        if (itemModel == null) {
            itemModel = getModel().bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        return itemModel;
    }

    protected IBakedModel getBlockBakedModel(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        EnumFacing frontFacing = EnumFacing.NORTH;
        if (tileEntity instanceof  ComponentTileEntity<?>) {
            frontFacing = ((ComponentTileEntity<?>) tileEntity).getFrontFacing();
        }
        return blockModels.computeIfAbsent(frontFacing, facing -> new EmissiveBakedModel(getModel().bake(
                TRSRTransformation.from(facing),
                DefaultVertexFormats.BLOCK,
                location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()))));
    }

    @Override
    public boolean isRaw() {
        return !CACHE.contains(modelLocation);
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
        return getItemBakedModel().getParticleTexture();
    }
}
