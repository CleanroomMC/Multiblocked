package io.github.cleanroommc.multiblocked.api.capability;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 *
 * @param <K> recipe info stored.
 */
@ZenClass("mods.multiblocked.capability.capability")
@ZenRegister
public abstract class MultiblockCapability<K> {
    @ZenProperty
    public final String name;

    public MultiblockCapability(String name) {
        this.name = name;
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
     * get candidates for rendering in jei.
     */
    public IBlockState[] getCandidates(IO io) {
        return new IBlockState[]{Blocks.GLASS.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(), Blocks.TNT.getDefaultState()};
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
