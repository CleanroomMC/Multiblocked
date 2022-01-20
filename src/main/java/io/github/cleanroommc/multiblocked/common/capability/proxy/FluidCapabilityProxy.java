package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Iterator;
import java.util.List;

public class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

    public FluidCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IFluidHandler getCapability() {
        //TODO MEK TANK only doesnt support the null side
        return getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
    }

    @Override
    public FluidStack copyInner(FluidStack content) {
        return content.copy();
    }

    @Override
    protected List<FluidStack> handleRecipeInner(IO io, Recipe recipe, List<FluidStack> left, boolean simulate) {
        IFluidHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<FluidStack> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                FluidStack fluidStack = iterator.next();
                FluidStack drained = capability.drain(fluidStack, !simulate);
                if (drained == null) continue;
                fluidStack.amount = fluidStack.amount - drained.amount;
                if (fluidStack.amount <= 0) {
                    iterator.remove();
                }
            }
        } else if (io == IO.OUT){
            while (iterator.hasNext()) {
                FluidStack fluidStack = iterator.next();
                int filled = capability.fill(fluidStack, !simulate);
                fluidStack.amount = fluidStack.amount - filled;
                if (fluidStack.amount <= 0) {
                    iterator.remove();
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

}
