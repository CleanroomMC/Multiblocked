package io.github.cleanroommc.multiblocked.api.framework.structure;

import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.util.Utils;

import java.util.Set;

public class MultiblockInstance implements INBTSerializable<NBTTagCompound> {

    private Multiblock multiblock;
    private BlockPos controllerPosition;
    private EnumFacing facing;
    private Set<BlockPos> positions;
    private Set<ChunkPos> chunkPositions;

    private MultiblockTileEntity internalTileEntity;

    private Status status = Status.ENABLED;

    @SideOnly(Side.CLIENT) private BlockPos nameTagPos;

    public MultiblockInstance() { }

    public MultiblockInstance(Multiblock multiblock, BlockPos controllerPosition, EnumFacing facing) {
        this.multiblock = multiblock;
        this.controllerPosition = controllerPosition;
        this.facing = facing;
        this.positions = new ObjectOpenHashSet<>();
        this.positions.add(this.controllerPosition);
        this.multiblock.getLayout().getPositions().keySet()
                .stream()
                .map(relPos -> Utils.rotate(this.controllerPosition, relPos, this.getFacing()))
                .forEach(this.positions::add);
        this.chunkPositions = getPositions().stream()
                .map(ChunkPos::new)
                .collect(ObjectArraySet::new, ObjectArraySet::add, ObjectArraySet::addAll);
        this.internalTileEntity = new MultiblockTileEntity(this);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getNameTagPos() {
        if (nameTagPos == null) {
            nameTagPos = Utils.rotate(controllerPosition, multiblock.getNameTag().getLeft(), facing);
        }
        return nameTagPos;
    }

    public void validate(World world) {
        if (!multiblock.check(world, controllerPosition, facing)) {
            MultiblockWorldSavedData.getOrCreate(world).removeMapping(world, controllerPosition);
        }
    }

    public boolean fastValidate(World world) {
        return multiblock.fastCheck(world, controllerPosition, facing);
    }

    public Multiblock getMultiblock() {
        return multiblock;
    }

    public BlockPos getControllerPosition() {
        return controllerPosition;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public MultiblockTileEntity getInternalTileEntity() {
        return internalTileEntity;
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }

    public Set<ChunkPos> getChunkPositions() {
        return chunkPositions;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void notify$() {

    }

    public boolean isInChunk(ChunkPos chunkPosition) {
        return this.chunkPositions.contains(chunkPosition);
    }

    public boolean isInChunk(int chunkX, int chunkZ) {
        return isInChunk(new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Multiblock", multiblock.getUnlocalizedName());
        nbt.setString("Facing", facing.getName());
        nbt.setTag("InternalTileEntity", this.internalTileEntity.serializeNBT());
        nbt.setInteger("x", controllerPosition.getX());
        nbt.setInteger("y", controllerPosition.getY());
        nbt.setInteger("z", controllerPosition.getZ());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        String multiblockKey = nbt.getString("Multiblock");
        this.multiblock = Multiblock.get(multiblockKey);
        if (this.multiblock == null) {
            Multiblocked.LOGGER.fatal("{} Multiblock no longer exists in the instance!", multiblockKey);
        }
        this.facing = EnumFacing.byName(nbt.getString("Facing"));
        this.internalTileEntity = new MultiblockTileEntity(this);
        internalTileEntity.deserializeNBT(nbt.getCompoundTag("InternalTileEntity"));
        this.controllerPosition = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        this.positions = new ObjectOpenHashSet<>();
        this.positions.add(this.controllerPosition);
        this.multiblock.getLayout().getPositions().keySet()
                .stream()
                .map(relPos -> Utils.rotate(this.controllerPosition, relPos, this.getFacing()))
                .forEach(this.positions::add);
        this.chunkPositions = getPositions().stream()
                .map(ChunkPos::new)
                .collect(ObjectArraySet::new, ObjectArraySet::add, ObjectArraySet::addAll);
    }

    public enum Status {

        ENABLED(TextFormatting.GREEN + "[Enabled]"),
        DISABLED(TextFormatting.RED + "[Disabled]"),
        ACTIVE(TextFormatting.GREEN + "[Active]"),
        INACTIVE(TextFormatting.RED + "[Inactive]"),
        UNLOADED("[Unloaded]");

        static final Status[] STATUSES = values();

        private final String string;

        Status(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }

    }

}
