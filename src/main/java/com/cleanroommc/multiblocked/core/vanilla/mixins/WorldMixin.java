package com.cleanroommc.multiblocked.core.vanilla.mixins;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(World.class)
public class WorldMixin {

    @Shadow @Final public Profiler profiler;

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

}
