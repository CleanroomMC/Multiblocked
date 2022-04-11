package com.cleanroommc.multiblocked.api.capability;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Objects;
import java.util.Set;

/**
 * The Proxy of a Capability {@link Capability}
 */
public abstract class CapCapabilityProxy<C, K> extends CapabilityProxy<K>{
    public final Capability<C> CAP;

    public CapCapabilityProxy(MultiblockCapability<? super K> capability, TileEntity tileEntity, Capability<C> cap) {
        super(capability, tileEntity);
        CAP = cap;
    }

    public C getCapability() {
        return super.getCapability(CAP);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CapCapabilityProxy && Objects.equals(getCapability(), ((CapCapabilityProxy<?, ?>) obj).getCapability());
    }
}
