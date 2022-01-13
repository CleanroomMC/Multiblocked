package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class FECapabilityProxy extends CapabilityProxy {
    public IEnergyStorage capability;

    public FECapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
        capability = tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public void handleRecipeInput(Recipe recipe) {

    }
}
