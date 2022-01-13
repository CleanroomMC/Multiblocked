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

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    protected abstract K copyInner(K content);

    @SuppressWarnings("unchecked")
    public final K copyContent(Object content) {
        return copyInner((K) content);
    }

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * create a proxy of this block.
     */
    public abstract CapabilityProxy<K> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * candidate of illustration in pattern.
     */
    public IBlockState getCandidate() {
        return null;
    }

}
