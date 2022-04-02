package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.ingredient.IGhostIngredientTarget;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PhantomFluidWidget extends TankWidget implements IGhostIngredientTarget {

    private Consumer<FluidStack> fluidStackUpdater;

    public PhantomFluidWidget(IFluidTank fluidTank, int x, int y) {
        super(fluidTank, x, y, false, false);
    }

    public PhantomFluidWidget setFluidStackUpdater(Consumer<FluidStack> fluidStackUpdater) {
        this.fluidStackUpdater = fluidStackUpdater;
        return this;
    }

    public static FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null)
                return fluidHandler.drain(Integer.MAX_VALUE, false);
        }
        return null;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack ingredientStack;
                if (ingredient instanceof FluidStack)
                    ingredientStack = (FluidStack) ingredient;
                else
                    ingredientStack = drainFrom(ingredient);

                if (ingredientStack != null) {
                    NBTTagCompound tagCompound = ingredientStack.writeToNBT(new NBTTagCompound());
                    writeClientAction(2, buffer -> buffer.writeCompoundTag(tagCompound));
                }

                if (isClientSideWidget && fluidStackUpdater != null) {
                    fluidStackUpdater.accept(ingredientStack);
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ItemStack itemStack = gui.entityPlayer.inventory.getItemStack().copy();
            if (!itemStack.isEmpty()) {
                itemStack.setCount(1);
                IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack resultFluid = fluidHandler.drain(Integer.MAX_VALUE, false);
                    fluidStackUpdater.accept(resultFluid);
                }
            } else {
                fluidStackUpdater.accept(null);
            }
        } else if (id == 2) {
            FluidStack fluidStack;
            try {
                fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fluidStackUpdater.accept(fluidStack);
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(1, buffer -> { });
            if (isClientSideWidget && fluidStackUpdater != null) {
                fluidStackUpdater.accept(null);
            }
            return this;
        }
        return null;
    }

}
