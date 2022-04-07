package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class HeatMekanismCapability extends MultiblockCapability<Double> {
    public static final HeatMekanismCapability CAP = new HeatMekanismCapability();

    private HeatMekanismCapability() {
        super("mek_heat", new Color(0xD9068D).getRGB());
    }

    @Override
    public Double defaultContent() {
        return 100d;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(Capabilities.HEAT_TRANSFER_CAPABILITY, tileEntity).isEmpty();
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
        return new NumberContentWidget().setContentTexture(new TextTexture("HE", color)).setUnit("Heat");
    }

    @Override
    public Double deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsDouble();
    }

    @Override
    public JsonElement serialize(Double aDouble, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(aDouble);
    }

    public static class HeatMekanismCapabilityProxy extends CapCapabilityProxy<IHeatTransfer, Double> {

        public HeatMekanismCapabilityProxy(TileEntity tileEntity) {
            super(HeatMekanismCapability.CAP, tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY);
        }

        @Override
        protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, boolean simulate) {
            Set<IHeatTransfer> capabilities = getCapability();
            double sum = left.stream().reduce(0d, Double::sum);
            for (IHeatTransfer capability : capabilities) {
                if (capability.getTemp() <= 0) continue;
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
            return left;
        }

    }
}
