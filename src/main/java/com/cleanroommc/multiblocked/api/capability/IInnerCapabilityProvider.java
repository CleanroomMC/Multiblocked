package com.cleanroommc.multiblocked.api.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public interface IInnerCapabilityProvider extends ICapabilityProvider {

    /**
     * inner capability used for recipe logic handling.
     */
    @Nullable
    default <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing);
    }


    /**
     * inner capability used for recipe logic handling with slotName.
     */
    default <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing, @Nullable String slotName) {
        return getInnerCapability(capability, facing);
    }

    /**
     * additional slot names
     */
    default Set<String> getSlotNames() {
        return Collections.emptySet();
    }
}
