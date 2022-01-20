package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class HeatMekanismCapabilityProxy extends CapabilityProxy<Double> {

    public HeatMekanismCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IHeatTransfer getCapability() {
        return getTileEntity().getCapability(Capabilities.HEAT_TRANSFER_CAPABILITY, null);
    }

    @Override
    protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, boolean simulate) {
        IHeatTransfer capability = getCapability();
        if (capability == null || capability.getTemp() <= 0) return left;
        double sum = left.stream().reduce(0d, Double::sum);
        if (io == IO.IN) {
            if (!simulate) {
                capability.transferHeatTo(-sum);
            }
        } else if (io == IO.OUT) {
            if (!simulate) {
                capability.transferHeatTo(sum);
            }
        }
        return null;
    }

    @Override
    protected Double copyInner(Double content) {
        return content;
    }
}
