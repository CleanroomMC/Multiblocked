package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

    public FluidCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IFluidHandler getCapability() {
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidStack searchingRecipe(IO io, Recipe recipe, FluidStack left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        if (io == IO.IN) {
            FluidStack drained = capability.drain(left, false);
            if (drained == null) return left;
            if (drained.amount == left.amount) return null;
            drained.amount = left.amount - drained.amount;
            return drained;
        } else if (io == IO.OUT){
            int filled = capability.fill(left, false);
            if (filled == left.amount) return null;
            left.amount = left.amount - filled;
            return left;
        }
        return left;
    }

    @Override
    protected FluidStack handleRecipeInput(IO io, Recipe recipe, FluidStack left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        FluidStack drained = capability.drain(left, true);
        if (drained == null) return left;
        if (drained.amount == left.amount) return null;
        drained.amount = left.amount - drained.amount;
        return drained;
    }

    @Override
    protected FluidStack handleRecipeOutput(IO io, Recipe recipe, FluidStack left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        int filled = capability.fill(left, false);
        if (filled == left.amount) return null;
        left.amount = left.amount - filled;
        return left;
    }

}
