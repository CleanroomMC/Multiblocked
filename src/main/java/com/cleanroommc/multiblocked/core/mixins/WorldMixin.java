package com.cleanroommc.multiblocked.core.mixins;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow @Final public Profiler profiler;

    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 4))
    private void afterUpdatingEntities(CallbackInfo ci) {
        this.profiler.startSection("multiblocks");
        if (!((World) (Object) this).isRemote) {
            List<ComponentTileEntity<?>> inValids = null;
            MultiblockWorldSavedData mbds = MultiblockWorldSavedData.getOrCreate((World) (Object) this);
            for (ComponentTileEntity<?> loading : mbds.getLoadings()) {
                if (loading.isInvalid()) {
                    if (inValids == null) {
                        inValids = new ArrayList<>();
                    }
                    inValids.add(loading);
                } else {
                    loading.update();
                }
            }
            if (inValids != null) {
                for (ComponentTileEntity<?> inValid : inValids) {
                    mbds.removeLoading(inValid.getPos());
                }
            }
        }
        this.profiler.endSection();
    }

    @Inject(method = "getTileEntity", at = @At(value = "HEAD"), cancellable = true)
    private void getTileEntity(BlockPos pos, CallbackInfoReturnable<TileEntity> cir) {
        if (this instanceof IThreadListener && !((IThreadListener) this).isCallingFromMinecraftThread() && !isBlockLoaded(pos)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getBlockState", at = @At(value = "HEAD"), cancellable = true)
    private void getBlockState(BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        if (this instanceof IThreadListener && !((IThreadListener) this).isCallingFromMinecraftThread() && !isBlockLoaded(pos)) {
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

}
