package io.github.cleanroommc.multiblocked.api.tile;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A TileEntity that defies all controller machines.
 *
 * Head of the multiblock.
 */
@ZenClass("mods.multiblocked.tile.Controller")
@ZenRegister
public class ControllerTileEntity extends ComponentTileEntity<ControllerDefinition>{
    public MultiblockState state;
    public Table<IO, MultiblockCapability<?>, List<CapabilityProxy<?>>> capabilities;
    public LongOpenHashSet parts;

    public ControllerTileEntity() {}

    @ZenMethod
    public boolean checkPattern() {
        if (state == null) return false;
        return definition.basePattern.checkPatternAt(state);
    }

    @ZenMethod
    @ZenGetter
    public boolean isFormed() {
        return state != null && state.isFormed();
    }

    public void updateFormed() {
    }

    /**
     * Called when its formed, server side only.
     */
    public void onStructureFormed() {
        // init capabilities
        Map<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> capabilityMap = state.getMatchContext().get("capabilities");
        if (capabilityMap != null) {
            capabilities = Tables.newCustomTable(new EnumMap<>(IO.class), Object2ObjectOpenHashMap::new);
            for (Map.Entry<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> entry : capabilityMap.entrySet()) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(entry.getKey()));
                if (tileEntity != null) {
                    entry.getValue().forEach((io,set)->{
                        for (MultiblockCapability<?> capability : set) {
                            if (capability.isBlockHasCapability(io, tileEntity)) {
                                if (!capabilities.contains(io, capability)) {
                                    capabilities.put(io, capability, new ArrayList<>());
                                }
                                capabilities.get(io, capability).add(capability.createProxy(io, tileEntity));
                            }
                        }
                    });
                }
            }
        }

        // init parts
        parts = state.getMatchContext().get("parts");
        if (parts != null) {
            for (Long pos : parts) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(pos));
                if (tileEntity instanceof PartTileEntity) {
                    ((PartTileEntity<?>) tileEntity).addedToController(this);
                }
            }
        }

        writeCustomData(-1, buffer -> buffer.writeBoolean(isFormed()));
        if (definition.structureFormed != null) {
            definition.structureFormed.apply(this);
        }
    }

    public void onStructureInvalid() {
        // invalid parts
        if (parts != null) {
            for (Long pos : parts) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(pos));
                if (tileEntity instanceof PartTileEntity) {
                    ((PartTileEntity<?>) tileEntity).removedFromController(this);
                }
            }
            parts = null;
        }
        capabilities = null;

        writeCustomData(-1, buffer -> buffer.writeBoolean(isFormed()));
        if (definition.structureInvalid != null) {
            definition.structureInvalid.apply(this);
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == -1) {
            if (buf.readBoolean()) {
                state = new MultiblockState(world, pos);
            } else {
                state = null;
            }
            scheduleChunkForRenderUpdate();
        } else {
            super.receiveCustomData(dataId, buf);

        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isFormed());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            state = new MultiblockState(world, pos);
        } else {
            state = null;
        }
        scheduleChunkForRenderUpdate();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        state = MultiblockWorldSavedData.getOrCreate(world).mapping.get(pos);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onRightClick(player, hand, facing, hitX, hitY, hitZ)) return true;
        if (!world.isRemote) {
            if (isFormed()) return false;
            if (state == null) state = new MultiblockState(world, pos);
            ItemStack held = player.getHeldItem(hand);
            if (definition.catalyst == null || held.isItemEqual(definition.catalyst)) {
                if (checkPattern()) { // formed
                    player.swingArm(hand);
                    ITextComponent formedMsg = new TextComponentTranslation("multiblocked.multiblock.formed", getLocalizedName());
                    player.sendStatusMessage(formedMsg, true);
                    if (!player.isCreative() && definition.consumeCatalyst) {
                        held.shrink(1);
                    }
                    MultiblockWorldSavedData.getOrCreate(world).addMapping(state);
                    onStructureFormed();
                    return true;
                }
            }
        }
        return false;
    }

}
