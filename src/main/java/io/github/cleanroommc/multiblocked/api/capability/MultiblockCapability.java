package io.github.cleanroommc.multiblocked.api.capability;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 *
 * @param <K> recipe info stored.
 */
public abstract class MultiblockCapability<K> {
    public final IO io;

    public MultiblockCapability(IO io) {
        this.io = io;
    }

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public abstract K copyContent(K content);

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull TileEntity tileEntity);

    /**
     * create a proxy of this block.
     */
    public abstract CapabilityProxy<K> createProxy(@Nonnull TileEntity tileEntity);

    /**
     * candidate of illustration in pattern.
     */
    public IBlockState getCandidate() {
        return null;
    }

}
