package com.cleanroommc.multiblocked.core.vanilla.mixins;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public class ChunkMixin {

    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private World world;

    // We want to be as quick as possible here
    @Inject(method = "setBlockState", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/World;captureBlockSnapshots:Z", remap = false))
    private void onAddingBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        if (this.world.getMinecraftServer() == null) return;
        this.world.getMinecraftServer().addScheduledTask(() -> {
           for (MultiblockState structure : MultiblockWorldSavedData.getOrCreate(this.world).getControllerInChunk(new ChunkPos(x, z))) {
               if (structure.isPosInCache(pos)) {
                   structure.onBlockStateChanged(pos);
               }
           }
        });
    }

}
