package com.cleanroommc.multiblocked.api.pattern;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.pattern.error.PatternError;
import com.cleanroommc.multiblocked.api.pattern.error.PatternStringError;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.PatternMatchContext;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketRemoveDisabledRendering;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiblockState {
    public final static PatternError UNLOAD_ERROR = new PatternStringError("multiblocked.pattern.error.chunk");
    public final static PatternError UNINIT_ERROR = new PatternStringError("multiblocked.pattern.error.init");

    public BlockPos pos;
    public IBlockState state;
    public TileEntity tileEntity;
    public boolean tileEntityInitialized;
    public PatternMatchContext matchContext;
    public Map<SimplePredicate, Integer> globalCount;
    public TraceabilityPredicate predicate;
    public IO io;
    public PatternError error;
    public final World world;
    public final BlockPos controllerPos;
    public ControllerTileEntity lastController;

    // persist
    public LongOpenHashSet cache;

    public MultiblockState(World world, BlockPos controllerPos) {
        this.world = world;
        this.controllerPos = controllerPos;
        this.error = UNINIT_ERROR;
    }

    public void clean() {
        this.matchContext = new PatternMatchContext();
        this.globalCount = new HashMap<>();
        cache = new LongOpenHashSet();
    }

    public boolean update(BlockPos posIn, TraceabilityPredicate predicate) {
        this.pos = posIn;
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
        this.predicate = predicate;
        this.error = null;
        if (!world.isBlockLoaded(posIn)) {
            error = UNLOAD_ERROR;
            return false;
        }
        return true;
    }

    public ControllerTileEntity getController() {
        TileEntity tileEntity = world.getTileEntity(controllerPos);
        if (tileEntity instanceof ControllerTileEntity) {
            return lastController = (ControllerTileEntity) tileEntity;
        }
        return null;
    }

    public boolean isFormed() {
        return error == null;
    }

    public void setError(PatternError error) {
        this.error = error;
        if (error != null) {
            error.setWorldState(this);
        }
    }

    public PatternMatchContext getMatchContext() {
        return matchContext;
    }

    public IBlockState getBlockState() {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.pos);
        }

        return this.state;
    }

    @Nullable
    public TileEntity getTileEntity() {
        if (this.tileEntity == null && !this.tileEntityInitialized) {
            this.tileEntity = this.world.getTileEntity(this.pos);
            this.tileEntityInitialized = true;
        }

        return this.tileEntity;
    }

    public BlockPos getPos() {
        return this.pos.toImmutable();
    }

    public IBlockState getOffsetState(EnumFacing face) {
        if (pos instanceof MutableBlockPos) {
            ((MutableBlockPos) pos).move(face);
            IBlockState blockState = world.getBlockState(pos);
            ((MutableBlockPos) pos).move(face.getOpposite());
            return blockState;
        }
        return world.getBlockState(this.pos.offset(face));
    }

    public World getWorld() {
        return world;
    }

    public void addPosCache(BlockPos pos) {
        cache.add(pos.toLong());
    }

    public boolean isPosInCache(BlockPos pos) {
        return cache.contains(pos.toLong());
    }

    public Collection<BlockPos> getCache() {
        return cache.stream().map(BlockPos::fromLong).collect(Collectors.toList());
    }

    public void onBlockStateChanged(BlockPos pos) {
        if (pos.equals(controllerPos)) {
            if (this.getMatchContext().containsKey("renderMask")) {
                MultiblockedNetworking.sendToWorld(new SPacketRemoveDisabledRendering(controllerPos), world);
            }
            if (lastController != null) {
                lastController.onStructureInvalid();
                if (lastController.hasOldBlock()) {
                    lastController.resetOldBlock(world, controllerPos);
                }
            }
            MultiblockWorldSavedData mbds = MultiblockWorldSavedData.getOrCreate(world);
            mbds.removeMapping(this);
            mbds.removeLoading(controllerPos);

        } else if (error != UNLOAD_ERROR) {
            ControllerTileEntity controller = getController();
            boolean hasRenderMask = getMatchContext().containsKey("renderMask");
            if (controller != null && !controller.checkPattern()) {
                controller.onStructureInvalid();
                if (controller.hasOldBlock()) {
                    if (hasRenderMask) {
                        MultiblockedNetworking.sendToWorld(new SPacketRemoveDisabledRendering(controllerPos), world);
                    }
                    MultiblockWorldSavedData.getOrCreate(world).removeLoading(controllerPos);
                    controller.resetOldBlock(world, controllerPos);
                }
                MultiblockWorldSavedData.getOrCreate(world).removeMapping(this);
            } else if (controller != null){
                controller.onStructureFormed();
            }
        }
    }

    public void onChunkLoad() {
        try {
            ControllerTileEntity controller = getController();
            if (controller != null) {
                if (controller.checkPattern()) {
                    if (!controller.needAlwaysUpdate()) {
                        MultiblockWorldSavedData.getOrCreate(world).addLoading(controller);
                    }
                    if (controller.getCapabilities() == null) {
                        controller.onStructureFormed();
                    }
                } else {
                    error = UNLOAD_ERROR;
                }
            }
        } catch (Throwable e) { // if controller loading failed.
            MultiblockWorldSavedData.getOrCreate(world).removeMapping(this);
            Multiblocked.LOGGER.error("An error while loading the controller world: {} pos: {}, {}", world.provider.getDimensionType().getSuffix(), controllerPos, e);
        }
    }

    public void onChunkUnload() {
        ControllerTileEntity controller = getController();
        if (controller != null) {
            error = UNLOAD_ERROR;
            if (!controller.needAlwaysUpdate()) {
                MultiblockWorldSavedData.getOrCreate(world).removeLoading(controllerPos);
            }
        } else {
            MultiblockWorldSavedData.getOrCreate(world).removeLoading(controllerPos);
        }
    }

    public void deserialize(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        cache = new LongOpenHashSet();
        for (int i = 0; i < size; i++) {
            cache.add(buffer.readVarLong());
        }
    }

    public void serialize(PacketBuffer buffer) {
        buffer.writeVarInt(cache.size());
        for (Long aLong : cache) {
            buffer.writeVarLong(aLong);
        }
    }
}
