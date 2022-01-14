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
    protected List<Integer> matchingRecipeInner(IO io, Recipe recipe, List<Integer> left) {
        if (io == IO.OUT) return null; // TODO should it always allow output even is full
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        int sum = left.stream().reduce(0, Integer::sum);
        int drained = capability.extractEnergy(capability.getEnergyStored(), true);
        return sum <= drained ? null : Collections.singletonList(sum - drained);
    }

    @Override
    protected List<Integer> handleRecipeInputInner(IO io, Recipe recipe, List<Integer> left) {
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        int sum = left.stream().reduce(0, Integer::sum);
        sum = sum - capability.extractEnergy(sum, false);
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    protected List<Integer> handleRecipeOutputInner(IO io, Recipe recipe, List<Integer> left) {
        IEnergyStorage capability = getCapability();
        if (capability == null) return left;
        int sum = left.stream().reduce(0, Integer::sum);
        sum = sum - capability.receiveEnergy(sum, false);
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

}
