package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.List;

public class FECapabilityProxy extends CapabilityProxy<Integer> {

    public FECapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IEnergyStorage getCapability() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        int sum = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            sum = sum - capability.extractEnergy(sum, simulate);
        } else if (io == IO.OUT) {
            sum = sum - capability.receiveEnergy(sum, simulate);
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

}
