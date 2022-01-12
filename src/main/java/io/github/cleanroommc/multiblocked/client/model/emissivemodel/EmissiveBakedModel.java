package io.github.cleanroommc.multiblocked.client.model.emissivemodel;

import io.github.cleanroommc.multiblocked.client.model.bakedpipeline.VertexBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Used to baked the model with emissive effect.
 *
 * Making the top layer emissive.
 */
@SideOnly(Side.CLIENT)
public class EmissiveBakedModel implements IBakedModel {
    private final Map<TextureAtlasSprite, Boolean> spriteCache;
    private final IBakedModel parent;
    private final EnumMap<EnumFacing, List<BakedQuad>> sideCache;
    private List<BakedQuad> noSideCache;

    public EmissiveBakedModel(IBakedModel parent) {
        this.parent = parent;
        this.spriteCache = new HashMap<>();
        this.sideCache = new EnumMap<>(EnumFacing.class);
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side == null && noSideCache != null) {
            return noSideCache;
        } else if (sideCache.containsKey(side)) {
            return sideCache.get(side);
        }
        List<BakedQuad> parentQuads = parent.getQuads(state, side, rand);
        List<BakedQuad> resultQuads = new LinkedList<>();
        for (BakedQuad quad : parentQuads) {
            TextureAtlasSprite sprite = quad.getSprite();
            boolean isEmissive = spriteCache.computeIfAbsent(sprite, MetadataSectionEmissive::isEmissive);
            if (isEmissive) {
                quad = reBakeEmissive(quad);
            }
            resultQuads.add(quad);
        }
        if (side == null) noSideCache = resultQuads;
        else sideCache.put(side, resultQuads);
        return resultQuads;
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
