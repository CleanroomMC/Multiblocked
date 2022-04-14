package com.cleanroommc.multiblocked.api.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IInnerCapabilityProvider extends ICapabilityProvider {

    /**
     * inner capability used for recipe logic handling.
     */
    @Nullable
    default <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing);
    }
}
