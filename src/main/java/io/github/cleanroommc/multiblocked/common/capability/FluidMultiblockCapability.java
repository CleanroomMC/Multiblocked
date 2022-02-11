package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.FluidContentWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import mekanism.common.base.FluidHandlerWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

public class FluidMultiblockCapability extends MultiblockCapability<FluidStack> {

    public FluidMultiblockCapability() {
        super("fluid", new Color(0x3C70EE).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public FluidStack copyInner(FluidStack content) {
        return content.copy();
    }


    @Override
    public FluidCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new FluidCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super FluidStack> createContentWidget() {
        return new FluidContentWidget();
    }

    public static class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

        public FluidCapabilityProxy(TileEntity tileEntity) {
            super(MultiblockCapabilities.FLUID, tileEntity);
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
