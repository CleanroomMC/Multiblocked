package io.github.cleanroommc.multiblocked.client.custom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class CustomBlockRendererDispatcher extends BlockRendererDispatcher {

    BlockRendererDispatcher parent;

    public CustomBlockRendererDispatcher(BlockRendererDispatcher parent) {
        super(parent.getBlockModelShapes(), Minecraft.getMinecraft().getBlockColors());
        this.parent = parent;
    }

    @Override
    public void renderBlockDamage(@Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull TextureAtlasSprite texture, @Nonnull IBlockAccess blockAccess) {
        if (state.getRenderType() == ComponentRenderer.COMPONENT_RENDER_TYPE) {
            ComponentRenderer.INSTANCE.renderBlockDamage(state, pos, texture, blockAccess);
        } else {
            parent.renderBlockDamage(state, pos, texture, blockAccess);
        }
    }

    @Override
    public boolean renderBlock(@Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull IBlockAccess blockAccess, @Nonnull BufferBuilder bufferBuilderIn) {
        if (state.getRenderType() == ComponentRenderer.COMPONENT_RENDER_TYPE) {
            try {
                return ComponentRenderer.INSTANCE.renderBlock(state, pos, blockAccess, bufferBuilderIn);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Multiblocked Component Renderer Tesselating block in world");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Component being tesselated");
                CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
                throw new ReportedException(crashreport);
            }
        }
        return parent.renderBlock(state, pos, blockAccess, bufferBuilderIn);
    }

    @Override
    public void renderBlockBrightness(@Nonnull IBlockState state, float brightness) {
        if (state.getRenderType() == ComponentRenderer.COMPONENT_RENDER_TYPE) {
            ComponentRenderer.INSTANCE.renderBlockBrightness(state, brightness);
        } else {
            parent.renderBlockBrightness(state, brightness);
        }
    }

    @Override
    @Nonnull
    public BlockModelRenderer getBlockModelRenderer() {
        return parent.getBlockModelRenderer();
    }

    @Override
    @Nonnull
    public IBakedModel getModelForState(@Nonnull IBlockState state) {
        return parent.getModelForState(state);
    }

    @Override
    @Nonnull
    public BlockModelShapes getBlockModelShapes() {
        return parent.getBlockModelShapes();
    }

}
