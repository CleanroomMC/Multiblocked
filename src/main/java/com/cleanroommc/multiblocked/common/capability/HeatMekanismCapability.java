package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import mekanism.api.IHeatTransfer;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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
    public BlockInfo[] getCandidates() {
        BlockStateMachine.MachineType[] machineTypes = new BlockStateMachine.MachineType[]{BlockStateMachine.MachineType.FUELWOOD_HEATER, BlockStateMachine.MachineType.RESISTIVE_HEATER};
        return Arrays.stream(machineTypes).map(type -> new BlockInfo(type.typeBlock.getBlock().getStateFromMeta(type.meta), type.create(), type.getStack())).toArray(BlockInfo[]::new);
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

        double lastTemp = Double.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            IHeatTransfer capability = getCapability();
            if (capability == null || capability.getTemp() <= 0) return false;
            if (lastTemp == capability.getTemp()) return false;
            lastTemp = capability.getTemp();
            return true;
        }
    }
}
