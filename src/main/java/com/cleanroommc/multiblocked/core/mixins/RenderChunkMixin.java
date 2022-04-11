package com.cleanroommc.multiblocked.core.mixins;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderChunk.class)
public class RenderChunkMixin {

    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"))
    private boolean injectRenderModelFlat(BlockRendererDispatcher blockRendererDispatcher, IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn) {
        MultiblockWorldSavedData.isBuildingChunk.set(true);
        if (MultiblockWorldSavedData.isModelDisabled(pos)) {
            MultiblockWorldSavedData.isBuildingChunk.set(false);
            return false;
        }
        boolean result = blockRendererDispatcher.renderBlock(state, pos, blockAccess, bufferBuilderIn);
        MultiblockWorldSavedData.isBuildingChunk.set(false);
        return result;
    }

}
