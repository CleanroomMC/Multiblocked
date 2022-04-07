package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EnergyGTCECapability extends MultiblockCapability<Long> {
    public static final EnergyGTCECapability CAP = new EnergyGTCECapability();

    public EnergyGTCECapability() {
        super("gtce_energy", new Color(0xF3C225).getRGB());
    }

    @Override
    public Long defaultContent() {
        return 256L;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, tileEntity).isEmpty();
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public EnergyCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new EnergyCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Long> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("EU", color)).setUnit("EU");
    }

    @Override
    public Long deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsLong();
    }

    @Override
    public JsonElement serialize(Long integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    public static class EnergyCapabilityProxy extends CapCapabilityProxy<IEnergyContainer, Long> {

        public EnergyCapabilityProxy(TileEntity tileEntity) {
            super(EnergyGTCECapability.CAP, tileEntity, GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        }

        @Override
        protected List<Long> handleRecipeInner(IO io, Recipe recipe, List<Long> left, boolean simulate) {
            Set<IEnergyContainer> capabilities = getCapability();
            long sum = left.stream().reduce(0L, Long::sum);
            for (IEnergyContainer capability : capabilities) {
                if (io == IO.IN) {
                    if (!simulate) {
                        capability.addEnergy(-Math.min(capability.getEnergyStored(), sum));
                    }
                    sum = sum - capability.getEnergyStored();
                } else if (io == IO.OUT) {
                    long canInput = capability.getEnergyCapacity() - capability.getEnergyStored();
                    if (!simulate) {
                        capability.addEnergy(Math.min(canInput, sum));
                    }
                    sum = sum - canInput;
                }
                if (sum <= 0) break;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

    }
}
