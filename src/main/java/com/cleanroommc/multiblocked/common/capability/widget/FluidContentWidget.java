package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.PhantomFluidWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TankWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluidContentWidget extends ContentWidget<FluidStack> {
    FluidTank fluidTank;

    @Override
    protected void onContentUpdate() {
        if (Multiblocked.isClient()) {
            List<String> tooltips = new ArrayList<>();
            tooltips.add(content.getFluid().getLocalizedName(content));
            tooltips.add(I18n.format("multiblocked.fluid.amount", content.amount, content.amount));
            tooltips.add(I18n.format("multiblocked.fluid.temperature", content.getFluid().getTemperature(content)));
            tooltips.add(I18n.format(content.getFluid().isGaseous(content) ? "multiblocked.fluid.state_gas" : "multiblocked.fluid.state_liquid"));
            setHoverTooltip(tooltips.stream().reduce((a, b) -> a + "\n" + b).orElse("fluid"));
        }
        if (fluidTank != null) {
            fluidTank.drainInternal(Integer.MAX_VALUE, true);
            fluidTank.setCapacity(content.amount);
            fluidTank.fillInternal(content.copy(), true);
        } else {
            addWidget(new TankWidget(fluidTank = new FluidTank(content.copy(), content.amount), 1, 1, false, false).setDrawHoverTips(false));
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        List<IGhostIngredientHandler.Target<?>> pattern = super.getPhantomTargets(ingredient);
        if (pattern != null && pattern.size() > 0) return pattern;

        if (!(ingredient instanceof FluidStack) && PhantomFluidWidget.drainFrom(ingredient) == null) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack content;
                if (ingredient instanceof FluidStack)
                    content = (FluidStack) ingredient;
                else
                    content = PhantomFluidWidget.drainFrom(ingredient);
                if (content != null) {
                    setContent(io, getJEIContent(content), chance, perTick);
                    if (onPhantomUpdate != null) {
                        onPhantomUpdate.accept(FluidContentWidget.this);
                    }
                }
            }
        });
    }

    @Override
    public FluidStack getJEIContent(Object content) {
        if (content instanceof FluidStack) {
            return new FluidStack(((FluidStack) content).getFluid(), this.content.amount);
        }
        return null;
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "Amount:"));
        dialog.addWidget(new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            content = new FluidStack(content, Integer.parseInt(number));
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.amount+""));
    }
}
