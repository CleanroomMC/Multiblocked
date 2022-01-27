package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content;

import io.github.cleanroommc.multiblocked.api.gui.widget.imp.TankWidget;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidContentWidget extends ContentWidget<FluidStack>{
    TankWidget tankWidget;

    @Override
    protected void onContentUpdate() {
        if (tankWidget != null) removeWidget(tankWidget);
        addWidget(tankWidget = new TankWidget(new FluidTank(content, content.amount), 0, 0, 20, 20, false, false).setAlwaysShowFull(true));
    }
    
}
