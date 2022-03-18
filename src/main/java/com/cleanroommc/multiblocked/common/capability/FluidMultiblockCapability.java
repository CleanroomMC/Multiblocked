package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.common.capability.widget.FluidContentWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.json.FluidStackTypeAdapter;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import mekanism.common.base.FluidHandlerWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
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

    @Override
    public FluidStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return FluidStackTypeAdapter.INSTANCE.deserialize(jsonElement, type, jsonDeserializationContext);
    }

    @Override
    public JsonElement serialize(FluidStack fluidStack, Type type, JsonSerializationContext jsonSerializationContext) {
        return FluidStackTypeAdapter.INSTANCE.serialize(fluidStack, type, jsonSerializationContext);
    }

    public static class FluidCapabilityProxy extends CapabilityProxy<FluidStack> {

        public FluidCapabilityProxy(TileEntity tileEntity) {
            super(MultiblockCapabilities.FLUID, tileEntity);
        }

        public IFluidHandler getCapability() {
            IFluidHandler fluidHandler = getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (Multiblocked.isModLoaded(Multiblocked.MODID_MEK) && fluidHandler instanceof FluidHandlerWrapper) {
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
