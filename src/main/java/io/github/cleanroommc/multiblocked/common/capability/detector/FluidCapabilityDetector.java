package io.github.cleanroommc.multiblocked.common.capability.detector;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityDetector;
import io.github.cleanroommc.multiblocked.common.capability.proxy.FluidCapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

public class FluidCapabilityDetector extends CapabilityDetector<FluidCapabilityProxy> {

    @Override
    public boolean isBlockHasCapability(@Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidCapabilityProxy createProxy(@Nonnull TileEntity tileEntity) {
        return new FluidCapabilityProxy(tileEntity);
    }
}
