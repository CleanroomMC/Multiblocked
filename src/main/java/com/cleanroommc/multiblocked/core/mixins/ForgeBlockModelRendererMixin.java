package com.cleanroommc.multiblocked.core.mixins;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeBlockModelRenderer.class)
public class ForgeBlockModelRendererMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    private static boolean injectRender(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
        if (MultiblockWorldSavedData.isModelDisabled(pos.offset(facing))) {
            return true;
        }
        return blockState.shouldSideBeRendered(blockAccess, pos, facing);
    }
    
}
