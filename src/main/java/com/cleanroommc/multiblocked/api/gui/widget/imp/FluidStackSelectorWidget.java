package com.cleanroommc.multiblocked.api.gui.widget.imp;


import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.function.Consumer;

public class FluidStackSelectorWidget extends WidgetGroup {
    private Consumer<FluidStack> onFluidStackUpdate;
    private final FluidTank handler;
    private final TextFieldWidget fluidField;
    private FluidStack fluid = new FluidStack(FluidRegistry.WATER, 0);

    public FluidStackSelectorWidget(WidgetGroup parent, int x, int y, int width) {
        super(x, y, width, 20);
        setClientSideWidget();
        fluidField = (TextFieldWidget) new TextFieldWidget(22, 0, width - 46, 20, null, s -> {
            if (s != null && !s.isEmpty()) {
                Fluid fluid = FluidRegistry.getFluid(s);
                if (fluid == null) {
                    this.fluid = new FluidStack(FluidRegistry.WATER, 0);
                    onUpdate();
                } else if (!this.fluid.isFluidEqual(new FluidStack(fluid, 1000))) {
                    this.fluid = new FluidStack(fluid, 1000);
                    onUpdate();
                }
            }
        }).setHoverTooltip("multiblocked.gui.tips.fluid_selector");

        addWidget(new PhantomFluidWidget(handler = new FluidTank(1000),1, 1)
                .setFluidStackUpdater(fluidStack -> {
                    setFluidStack(fluidStack);
                    onUpdate();
                }).setBackground(new ColorBorderTexture(1, -1)));
        addWidget(fluidField);

        addWidget(new ButtonWidget(width - 21, 0, 20, 20, null, cd -> {
            if (fluid.amount == 0) return;
            TextFieldWidget nbtField;
            new DialogWidget(parent, isClientSideWidget)
                    .setOnClosed(this::onUpdate)
                    .addWidget(nbtField = new TextFieldWidget(10, 10, parent.getSize().width - 50, 20, null, s -> {
                        try {
                            fluid.tag = JsonToNBT.getTagFromJson(s);
                            onUpdate();
                        } catch (NBTException ignored) {

                        }
                    }));
            if (fluid.tag != null) {
                nbtField.setCurrentString(fluid.tag.toString());
            }
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("NBT", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.fluid_tag"));
    }

    public FluidStack getFluidStack() {
        return fluid;
    }

    public FluidStackSelectorWidget setFluidStack(FluidStack fluidStack) {
        fluid = fluidStack == null ? new FluidStack(FluidRegistry.WATER, 0) : fluidStack;
        fluid = fluid.copy();
        fluid.amount = fluidStack == null ? 0 : 1000;
        handler.setFluid(fluid);
        fluidField.setCurrentString(fluid.getFluid().getName());
        return this;
    }

    public FluidStackSelectorWidget setOnFluidStackUpdate(Consumer<FluidStack> onFluidStackUpdate) {
        this.onFluidStackUpdate = onFluidStackUpdate;
        return this;
    }

    private void onUpdate() {
        handler.setFluid(fluid);
        if (onFluidStackUpdate != null) {
            onFluidStackUpdate.accept(fluid);
        }
    }
}
