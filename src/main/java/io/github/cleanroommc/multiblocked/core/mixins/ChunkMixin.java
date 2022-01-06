package io.github.cleanroommc.multiblocked.core.mixins;

import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.cleanroommc.multiblocked.api.framework.structure.MultiblockInstance;

@Mixin(Chunk.class)
public class ChunkMixin {

    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private World world;

    // We want to be as quick as possible here
    @Inject(method = "setBlockState", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/World;captureBlockSnapshots:Z"))
    private void onAddingBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        final World world = this.world;
        this.world.getMinecraftServer().addScheduledTask(() -> {
           for (MultiblockInstance instance : MultiblockWorldSavedData.getOrCreate(this.world).getInstances()) {
               if (instance.isInChunk(this.x, this.z) && instance.getPositions().contains(pos)) {
                   instance.validate(world);
                   break;
               }
           }
        });
    }

    @Inject(method = "getTileEntity", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void intercept$getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType creationMode, CallbackInfoReturnable<TileEntity> cir) {
        TileEntity returnValue = cir.getReturnValue();
        if (returnValue == null) {
            for (MultiblockInstance instance : MultiblockWorldSavedData.getOrCreate(this.world).getInstances()) {
                if (instance.isInChunk(this.x, this.z) && instance.getPositions().contains(pos)) {
                    cir.setReturnValue(instance.getInternalTileEntity());
                    return;
                }
            }
        }
    }

}
