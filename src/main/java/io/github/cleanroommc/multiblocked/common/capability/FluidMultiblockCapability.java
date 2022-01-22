package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import mekanism.common.base.FluidHandlerWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class FluidMultiblockCapability extends MultiblockCapability {

    public FluidMultiblockCapability() {
        super("fluid");
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new FluidCapabilityProxy(tileEntity);
    }

    public static class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

        public FluidCapabilityProxy(TileEntity tileEntity) {
            super(tileEntity);
        }

        public IFluidHandler getCapability() {
            IFluidHandler fluidHandler = getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (Multiblocked
                    .isModLoaded("mekanism") && fluidHandler instanceof FluidHandlerWrapper) {
                return getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
            }
            return fluidHandler;
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
}
