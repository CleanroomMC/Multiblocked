package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.NumberContentWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;

public class HeatMekanismCapability extends MultiblockCapability<Double> {
    public static final HeatMekanismCapability CAP = new HeatMekanismCapability();

    private HeatMekanismCapability() {
        super("mek_heat", new Color(0xD9068D).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(Capabilities.HEAT_TRANSFER_CAPABILITY, null);
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public HeatMekanismCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new HeatMekanismCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Double> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new ColorRectTexture(this.color)).setUnit("Heat");
    }

    public static class HeatMekanismCapabilityProxy extends CapabilityProxy<Double> {

        public HeatMekanismCapabilityProxy(TileEntity tileEntity) {
            super(HeatMekanismCapability.CAP, tileEntity);
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

    }
}
