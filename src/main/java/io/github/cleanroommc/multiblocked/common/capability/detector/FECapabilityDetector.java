package io.github.cleanroommc.multiblocked.common.capability.detector;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityDetector;
import io.github.cleanroommc.multiblocked.common.capability.proxy.FECapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;

public class FECapabilityDetector extends CapabilityDetector<FECapabilityProxy> {

    @Override
    public boolean isBlockHasCapability(@Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public FECapabilityProxy createProxy(@Nonnull TileEntity tileEntity) {
        return new FECapabilityProxy(tileEntity);
    }
}
