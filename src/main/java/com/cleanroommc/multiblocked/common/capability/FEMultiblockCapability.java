package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FEMultiblockCapability extends MultiblockCapability<Integer> {

    public FEMultiblockCapability() {
        super("forge_energy", new Color(0xCB0000).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 500;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        IEnergyStorage capability = getCapability(tileEntity);
        return capability != null && (io == IO.IN && capability.canExtract() ||
                        io == IO.OUT && capability.canReceive() ||
                        io == IO.BOTH && capability.canReceive() && capability.canExtract());
    }

    public IEnergyStorage getCapability(TileEntity tileEntity) {
        for (EnumFacing facing : EnumFacing.values()) {
            IEnergyStorage energyStorage = tileEntity.getCapability(CapabilityEnergy.ENERGY, facing);
            if (energyStorage != null) return energyStorage;
        }
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public FECapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new FECapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("FE", color)).setUnit("FE");
    }

    @Override
    public Integer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    public static class FECapabilityProxy extends CapabilityProxy<Integer> {

        public FECapabilityProxy(TileEntity tileEntity) {
            super(MultiblockCapabilities.FE, tileEntity);
        }

        public IEnergyStorage getCapability() {
            return MultiblockCapabilities.FE.getCapability(getTileEntity());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FECapabilityProxy && Objects.equals(getCapability(), ((FECapabilityProxy) obj).getCapability());
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IEnergyStorage capability = getCapability();
            if (capability == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                sum = sum - capability.extractEnergy(sum, simulate);
            } else if (io == IO.OUT) {
                sum = sum - capability.receiveEnergy(sum, simulate);
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

    }
}
