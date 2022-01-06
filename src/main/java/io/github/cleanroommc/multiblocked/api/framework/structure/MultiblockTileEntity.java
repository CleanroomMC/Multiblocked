package io.github.cleanroommc.multiblocked.api.framework.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * A TileEntity that defies everything a TileEntity represents.
 *
 * This isn't going to be in-world. But the parent MultiblockInstance would.
 */
public class MultiblockTileEntity extends TileEntity implements ITickable {

    @Override
    public void update() {

    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return super.getCapability(capability, facing);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

}
