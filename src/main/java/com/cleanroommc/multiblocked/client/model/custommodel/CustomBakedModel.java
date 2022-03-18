package com.cleanroommc.multiblocked.client.model.custommodel;

import com.cleanroommc.multiblocked.client.model.bakedpipeline.VertexBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to baked the model with emissive effect. or multi-layer
 *
 * Making the top layer emissive.
 */
@SideOnly(Side.CLIENT)
public class CustomBakedModel implements IBakedModel {
    private final IBakedModel parent;
    private final Table<BlockRenderLayer,EnumFacing, List<BakedQuad>> sideCache;
    private final EnumMap<BlockRenderLayer, List<BakedQuad>> noSideCache;

    public CustomBakedModel(IBakedModel parent) {
        this.parent = parent;
        this.noSideCache = new EnumMap<>(BlockRenderLayer.class);
        this.sideCache = Tables.newCustomTable(new EnumMap<>(BlockRenderLayer.class), ()-> new EnumMap<>(EnumFacing.class));
    }

    public boolean shouldRenderInLayer(@Nullable IBlockState state, long rand) {
        if (!getQuads(state, null, rand).isEmpty()) return true;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!getQuads(state, side, rand).isEmpty()) return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer currentLayer = MinecraftForgeClient.getRenderLayer();
        currentLayer = currentLayer == null ? BlockRenderLayer.CUTOUT : currentLayer;
        if (side == null) {
            if (!noSideCache.containsKey(currentLayer)) {
                reBake(currentLayer, state, null, rand);
            }
            return noSideCache.get(currentLayer);
        } else {
            if (!sideCache.contains(currentLayer, side)) {
                reBake(currentLayer, state, side, rand);
            }
            return sideCache.get(currentLayer, side);
        }
    }

    public void reBake(BlockRenderLayer currentLayer, @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> parentQuads = parent.getQuads(state, side, rand);
        List<BakedQuad> resultQuads = new LinkedList<>();
        for (BakedQuad quad : parentQuads) {
            TextureAtlasSprite sprite = quad.getSprite();
            boolean isEmissive = MetadataSectionEmissive.isEmissive(sprite);
            BlockRenderLayer layer = MetadataSectionEmissive.getLayer(sprite);
            layer = layer == null ? BlockRenderLayer.CUTOUT : layer;
            if (currentLayer != layer) continue;
            if (isEmissive) {
                quad = reBakeEmissive(quad);
            }
            resultQuads.add(quad);
        }
        if (side == null) noSideCache.put(currentLayer, resultQuads);
        else sideCache.put(currentLayer, side, resultQuads);
    }

    public static BakedQuad reBakeEmissive(BakedQuad quad) {
        VertexBuilder builder = new VertexBuilder(quad.getFormat(), quad.getSprite());
        quad.pipe(builder);
        VertexFormat format = builder.vertexFormat;
        if (format == DefaultVertexFormats.ITEM) { // ITEM is convertable to BLOCK (replace normal+padding with lmap)
            format = DefaultVertexFormats.BLOCK;
        } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is unknown, add TEX_2S if it does not exist
            format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
        }

        UnpackedBakedQuad.Builder unpackedBuilder = new UnpackedBakedQuad.Builder(format);
        unpackedBuilder.setQuadOrientation(builder.quadOrientation);
        unpackedBuilder.setQuadTint(builder.quadTint);
        unpackedBuilder.setApplyDiffuseLighting(builder.applyDiffuseLighting);
        unpackedBuilder.setTexture(builder.sprite);

        Vector2f[] uvs = builder.uvs();
        for (int v = 0; v < 4; v++) {
            for (int i = 0; i < format.getElementCount(); i++) {
                VertexFormatElement ele = format.getElement(i);
                //Stuff for Light or UV
                if (ele.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                    unpackedBuilder.put(i, 1, 1, 1, 1);
                } else if (ele.getUsage() == VertexFormatElement.EnumUsage.UV) {
                    if (ele.getIndex() == 1) {
                        unpackedBuilder.put(i, ((float) 15 * 0x20) / 0xFFFF, ((float) 15 * 0x20) / 0xFFFF); // lighting map (15, 15)
                    } else if (ele.getIndex() == 0) {
                        Vector2f uv = uvs[v];
                        unpackedBuilder.put(i, uv.x, uv.y, 0, 1);
                    }
                } else {
                    unpackedBuilder.put(i, builder.data.get(ele.getUsage()).get(v));
                }
            }
        }
        return unpackedBuilder.build();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return parent.isBuiltInRenderer();
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return parent.getItemCameraTransforms();
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return parent.getOverrides();
    }

    @Override
    public boolean isAmbientOcclusion(@Nonnull IBlockState state) {
        return parent.isAmbientOcclusion();
    }

    @Override
    @Nonnull
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
        return parent.handlePerspective(cameraTransformType);
    }
}
