package io.github.cleanroommc.multiblocked.client.model.modelfactories;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.model.ModelFactory;
import io.github.cleanroommc.multiblocked.client.renderer.BlockComponentRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockComponentBakedModel implements IBakedModel {

    public static final BlockComponentBakedModel INSTANCE = new BlockComponentBakedModel();
    private final ThreadLocal<TextureAtlasSprite> particle;

    private BlockComponentBakedModel() {
        this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();
        ComponentTileEntity component;
        if (state instanceof IExtendedBlockState) {
            component = ((IExtendedBlockState)state).getValue(BlockComponent.COMPONENT_PROPERTY);
        } else {
            component = FrameModelItemOverride.INSTANCE.component.get();
        }
        if (component != null) {
            return BlockComponentRenderer.renderComponent(component, side, MinecraftForgeClient.getRenderLayer());
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return particle.get();
    }

    @Override
    @Nonnull
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, ModelFactory.getBlockTransform(cameraTransformType).getMatrix());
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return FrameModelItemOverride.INSTANCE;
    }

    private static class FrameModelItemOverride extends ItemOverrideList {

        private static final FrameModelItemOverride INSTANCE = new FrameModelItemOverride();

        private final ThreadLocal<ComponentTileEntity> component = ThreadLocal.withInitial(() -> null);

        public FrameModelItemOverride() {
            super(Collections.emptyList());
        }

        @Override
        @Nonnull
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (!stack.isEmpty()) {
                BlockComponent blockComponent = (BlockComponent) ((ItemBlock) stack.getItem()).getBlock();
                this.component.set(blockComponent.component);
            } else {
                this.component.set(null);
            }
            return originalModel;
        }
    }
}
