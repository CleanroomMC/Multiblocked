package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

    public FluidCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IFluidHandler getCapability() {
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidStack copyInner(FluidStack content) {
        return content.copy();
    }

    @Override
    protected List<FluidStack> matchingRecipeInner(IO io, Recipe recipe, List<FluidStack> left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<FluidStack> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                FluidStack fluidStack = iterator.next();
                FluidStack drained = capability.drain(fluidStack, false);
                if (drained == null) continue;
                fluidStack.amount = fluidStack.amount - drained.amount;
                if (fluidStack.amount <= 0) {
                    iterator.remove();
                }
            }
        } else if (io == IO.OUT){
            while (iterator.hasNext()) {
                FluidStack fluidStack = iterator.next();
                int filled = capability.fill(fluidStack, false);
                fluidStack.amount = fluidStack.amount - filled;
                if (fluidStack.amount <= 0) {
                    iterator.remove();
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    protected List<FluidStack> handleRecipeInputInner(IO io, Recipe recipe, List<FluidStack> left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<FluidStack> iterator = left.iterator();
        while (iterator.hasNext()) {
            FluidStack fluidStack = iterator.next();
            FluidStack drained = capability.drain(fluidStack, true);
            if (drained == null) continue;
            fluidStack.amount = fluidStack.amount - drained.amount;
            if (fluidStack.amount <= 0) {
                iterator.remove();
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    protected List<FluidStack> handleRecipeOutputInner(IO io, Recipe recipe, List<FluidStack> left) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<FluidStack> iterator = left.iterator();
        while (iterator.hasNext()) {
            FluidStack fluidStack = iterator.next();
            int filled = capability.fill(fluidStack, true);
            fluidStack.amount = fluidStack.amount - filled;
            if (fluidStack.amount <= 0) {
                iterator.remove();
            }
        }
        return left.isEmpty() ? null : left;
    }

}
