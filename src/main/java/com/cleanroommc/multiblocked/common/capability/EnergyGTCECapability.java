package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return getCapability(tileEntity) != null;
    }

    public IEnergyContainer getCapability(@Nonnull TileEntity tileEntity) {
        for (EnumFacing facing : EnumFacing.values()) {
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facing);
            if (energyContainer != null) return energyContainer;
        }
        return tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
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

    public static class EnergyCapabilityProxy extends CapabilityProxy<Long> {

        public EnergyCapabilityProxy(TileEntity tileEntity) {
            super(EnergyGTCECapability.CAP, tileEntity);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EnergyCapabilityProxy && Objects.equals(getCapability(), ((EnergyCapabilityProxy) obj).getCapability());
        }

        public IEnergyContainer getCapability() {
            return EnergyGTCECapability.CAP.getCapability(getTileEntity());
        }

        @Override
        protected List<Long> handleRecipeInner(IO io, Recipe recipe, List<Long> left, boolean simulate) {
            IEnergyContainer capability = getCapability();
            if (capability == null) return left;
            long sum = left.stream().reduce(0L, Long::sum);
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
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

    }
}
