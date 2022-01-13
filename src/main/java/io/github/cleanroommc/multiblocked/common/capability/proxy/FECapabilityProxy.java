package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class FECapabilityProxy extends CapabilityProxy<Integer> {

    public FECapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IEnergyStorage getCapability() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    protected Integer matchingRecipeInner(IO io, Recipe recipe, Integer left) {
        if (io == IO.OUT) return null; // TODO should it always allow output even is full
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        int drained = capability.extractEnergy(capability.getEnergyStored(), true);
        return left <= drained ? null : left - drained;
    }

    @Override
    protected Integer handleRecipeInputInner(IO io, Recipe recipe, Integer left) {
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        left = left - capability.extractEnergy(left, false);
        return left <= 0 ? null : left;
    }

    @Override
    protected Integer handleRecipeOutputInner(IO io, Recipe recipe, Integer left) {
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        left = left - capability.receiveEnergy(left, false);
        return left <= 0 ? null : left;
    }


}
