package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class GasMekanismCapability extends MultiblockCapability {
    public static final GasMekanismCapability CAP = new GasMekanismCapability();

    private GasMekanismCapability() {
        super("mek_gas");
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
    }

    @Override
    public GasMekanismCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new GasMekanismCapabilityProxy(tileEntity);
    }

    public static class GasMekanismCapabilityProxy extends CapabilityProxy<GasStack> {

        public GasMekanismCapabilityProxy(TileEntity tileEntity) {
            super(tileEntity);
        }

        public IGasHandler getCapability() {
            return getTileEntity().getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
        }

        @Override
        protected List<GasStack> handleRecipeInner(IO io, Recipe recipe, List<GasStack> left, boolean simulate) {
            IGasHandler capability = getCapability();
            if (capability == null) return left;
            Iterator<GasStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    GasStack gasStack = iterator.next();
                    for (EnumFacing facing : EnumFacing.values()) {
                        if (capability.canDrawGas(facing, gasStack.getGas())) {
                            GasStack drain = capability.drawGas(facing, gasStack.amount, !simulate);
                            if (drain == null) continue;
                            gasStack.amount -= drain.amount;
                        }
                        if (gasStack.amount <= 0) break;
                    }
                    if (gasStack.amount <= 0) {
                        iterator.remove();
                    }
                }
            } else if (io == IO.OUT){
                while (iterator.hasNext()) {
                    GasStack gasStack = iterator.next();
                    for (EnumFacing facing : EnumFacing.values()) {
                        if (capability.canReceiveGas(facing, gasStack.getGas())) {
                            gasStack.amount -= capability.receiveGas(facing, gasStack, !simulate);
                        }
                        if (gasStack.amount <= 0) break;
                    }
                    if (gasStack.amount <= 0) {
                        iterator.remove();
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        protected GasStack copyInner(GasStack content) {
            return content.copy();
        }
    }
}
