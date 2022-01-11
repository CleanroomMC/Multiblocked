package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.client.model.ModelFactory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

public interface ICustomItemRenderer extends IBakedModel {

    void renderItem(ItemStack stack);

    @Override
    @Nonnull
    default Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull TransformType cameraTransformType) {
        TRSRTransformation transformation = ModelFactory.getBlockTransform(cameraTransformType);
        return transformation == null ? IBakedModel.super.handlePerspective(cameraTransformType) : Pair.of(this, transformation.getMatrix());
    }

    @Override
    @Nonnull
    default List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return Collections.emptyList();
    }

    @Override
    default boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    @Nonnull
    default ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    default boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    default boolean isGui3d() {
        return true;
    }
}
