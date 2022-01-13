package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.common.capability.proxy.FluidCapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

public class FluidMultiblockCapability extends MultiblockCapability<FluidStack> {

    public FluidMultiblockCapability(IO io) {
        super(io);
    }

    @Override
    public FluidStack copyContent(FluidStack content) {
        return content.copy();
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidCapabilityProxy createProxy(@Nonnull TileEntity tileEntity) {
        return new FluidCapabilityProxy(tileEntity);
    }
}
