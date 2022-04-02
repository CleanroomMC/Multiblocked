package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.model.ModelFactory;
import com.cleanroommc.multiblocked.client.model.custommodel.CustomBakedModel;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cleanroommc.multiblocked.client.ClientProxy.registerNeeds;

public class IModelRenderer implements IRenderer {

    protected static final Set<ResourceLocation> CACHE = new HashSet<>();

    public final ResourceLocation modelLocation;
    @SideOnly(Side.CLIENT)
    protected transient IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    protected transient Map<EnumFacing, CustomBakedModel> blockModels;

    public IModelRenderer(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        checkRegister();
    }

    public IModelRenderer checkRegister() {
        if (Multiblocked.isClient()) {
            if (isRaw()) {
                registerNeeds.add(this);
                CACHE.add(modelLocation);
            }
            blockModels = new EnumMap<>(EnumFacing.class);
        }
        return this;
    }

    @SideOnly(Side.CLIENT)
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
    @SideOnly(Side.CLIENT)
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        CustomBakedModel model = getBlockBakedModel(pos, blockAccess);
        if (!model.shouldRenderInLayer(state, MathHelper.getPositionRandom(pos))) return false;
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        blockModelRenderer.renderModel(blockAccess, model, state, pos, buffer, true);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        IBakedModel bakedModel = net.minecraftforge.client.ForgeHooksClient.getDamageModel(getBlockBakedModel(pos, blockAccess), texture, state, blockAccess, pos);
        blockModelRenderer.renderModel(blockAccess, bakedModel, state, pos, Tessellator.getInstance().getBuffer(), true);
    }

    @SideOnly(Side.CLIENT)
    protected IBakedModel getItemBakedModel() {
        if (itemModel == null) {
            itemModel = getModel().bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        return itemModel;
    }

    @SideOnly(Side.CLIENT)
    protected CustomBakedModel getBlockBakedModel(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        EnumFacing frontFacing = EnumFacing.NORTH;
        if (tileEntity instanceof  ComponentTileEntity<?>) {
            frontFacing = ((ComponentTileEntity<?>) tileEntity).getFrontFacing();
        }
        return blockModels.computeIfAbsent(frontFacing, facing -> new CustomBakedModel(getModel().bake(
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
        blockModels.clear();
        itemModel = null;
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
