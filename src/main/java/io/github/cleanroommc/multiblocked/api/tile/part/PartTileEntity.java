package io.github.cleanroommc.multiblocked.api.tile.part;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IBlockPos;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A TileEntity that defies the part of multi.
 *
 * part of the multiblock.
 */
@ZenClass("mods.multiblocked.tile.Part")
@ZenRegister
public abstract class PartTileEntity<T extends PartDefinition> extends ComponentTileEntity<T> {

    public Set<BlockPos> controllerPos = new HashSet<>();

    @Override
    public boolean isFormed() {
        for (BlockPos blockPos : controllerPos) {
            TileEntity controller = world.getTileEntity(blockPos);
            if (controller instanceof ControllerTileEntity && ((ControllerTileEntity) controller).isFormed()) {
                return true;
            }
        }
        return false;
    }

    public boolean canShared() {
        return definition.canShared;
    }

    @ZenMethod
    @ZenGetter("controllers")
    public List<ControllerTileEntity> getControllers() {
        List<ControllerTileEntity> result = new ArrayList<>();
        for (BlockPos blockPos : controllerPos) {
            TileEntity controller = world.getTileEntity(blockPos);
            if (controller instanceof ControllerTileEntity && ((ControllerTileEntity) controller).isFormed()) {
                result.add((ControllerTileEntity) controller);
            }
        }
        return result;
    }

    public void addedToController(@Nonnull ControllerTileEntity controller){
        if (controllerPos.add(controller.getPos())) {
            writeCustomData(-1, this::writeControllersToBuffer);
            if (definition.partAddedToMulti != null) {
                definition.partAddedToMulti.apply(this, controller);
            }
        }
    }

    public void removedFromController(@Nonnull ControllerTileEntity controller){
        if (controllerPos.remove(controller.getPos())) {
            writeCustomData(-1, this::writeControllersToBuffer);
            if (definition.partRemovedFromMulti != null) {
                definition.partRemovedFromMulti.apply(this, controller);
            }
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeControllersToBuffer(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readControllersFromBuffer(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == -1) {
            readControllersFromBuffer(buf);
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        for (MultiblockState state : MultiblockWorldSavedData.getOrCreate(world).getControllerInChunk(new ChunkPos(pos))) {
            if(state.isPosInCache(pos)) {
                controllerPos.add(state.controllerPos);
            }
        }
    }

    private void writeControllersToBuffer(PacketBuffer buffer) {
        buffer.writeVarInt(controllerPos.size());
        for (BlockPos pos : controllerPos) {
            buffer.writeBlockPos(pos);
        }
    }

    private void readControllersFromBuffer(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        controllerPos.clear();
        for (int i = size; i > 0; i--) {
            controllerPos.add(buffer.readBlockPos());
        }
    }

    public static class PartSimpleTileEntity extends PartTileEntity<PartDefinition> {

    }

}
