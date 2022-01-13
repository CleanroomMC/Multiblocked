package io.github.cleanroommc.multiblocked.api.capability;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 */
public abstract class CapabilityDetector<T extends CapabilityProxy> {

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull TileEntity tileEntity);

    /**
     * create a proxy of this block.
     */
    public abstract T createProxy(@Nonnull TileEntity tileEntity);

    /**
     * candidate of illustration in pattern.
     */
    public IBlockState getCandidate() {
        return null;
    }

}
