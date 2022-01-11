package io.github.cleanroommc.multiblocked.api.pattern;

import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
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
    public final static PatternError UNLOAD_ERROR = new PatternStringError("chunk_unload");

    protected BlockPos pos;
    protected IBlockState state;
    protected TileEntity tileEntity;
    protected boolean tileEntityInitialized;
    protected PatternMatchContext matchContext;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount;
    protected TraceabilityPredicate predicate;
    protected PatternError error;
    protected ControllerTileEntity controller;
    protected final World world;
    protected final BlockPos controllerPos;

    // persist
    protected LongOpenHashSet cache;

    public MultiblockState(World world, BlockPos controllerPos) {
        this.world = world;
        this.controllerPos = controllerPos;
    }

    public void clean() {
        this.matchContext = new PatternMatchContext();
        this.globalCount = new HashMap<>();
        this.layerCount = new HashMap<>();
        cache = new LongOpenHashSet();
    }

    public void update(BlockPos posIn, TraceabilityPredicate predicate) {
        this.pos = posIn;
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
        this.predicate = predicate;
        this.error = null;
    }

    public ControllerTileEntity getController() {
        if (controller != null) return controller;
        TileEntity tileEntity = world.getTileEntity(controllerPos);
        if (tileEntity instanceof ControllerTileEntity) {
            controller = (ControllerTileEntity) tileEntity;
        }
        return controller;
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
            if (getController() != null) controller.onStructureInvalid();
            MultiblockWorldSavedData.getOrCreate(world).removeMapping(this);
        } else if (getController() != null && error != UNLOAD_ERROR) {
            if (!controller.checkPattern()) {
                controller.onStructureInvalid();
                MultiblockWorldSavedData.getOrCreate(world).removeMapping(this);
            }
        }
    }

    public void onChunkLoad() {
        if (getController() != null) {
            if (!controller.checkPattern()) {
                error = UNLOAD_ERROR;
            } else {
                MultiblockWorldSavedData.getOrCreate(world).addLoading(controller);
            }
        }
    }

    public void onChunkUnload() {
        if (getController() != null) {
            error = UNLOAD_ERROR;
            MultiblockWorldSavedData.getOrCreate(world).removeLoading(controller);
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
