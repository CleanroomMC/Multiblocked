package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.common.capability.proxy.FECapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class FEMultiblockCapability extends MultiblockCapability<Integer> {

    public FEMultiblockCapability(IO io) {
        super(io);
    }

    @Override
    public Integer copyContent(Integer content) {
        return content;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull TileEntity tileEntity) {
        IEnergyStorage capability = tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
        return capability != null && (io == IO.IN && capability.canExtract() ||
                        io == IO.OUT && capability.canReceive() ||
                        io == IO.BOTH && capability.canReceive() && capability.canExtract());
    }

    @Override
    public FECapabilityProxy createProxy(@Nonnull TileEntity tileEntity) {
        return new FECapabilityProxy(tileEntity);
    }
}
