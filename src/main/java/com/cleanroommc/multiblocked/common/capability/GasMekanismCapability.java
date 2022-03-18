package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.common.capability.widget.GasStackWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class GasMekanismCapability extends MultiblockCapability<GasStack> {
    public static final GasMekanismCapability CAP = new GasMekanismCapability();

    private GasMekanismCapability() {
        super("mek_gas", new Color(0x85909E).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
    }

    @Override
    public GasStack copyInner(GasStack content) {
        return content.copy();
    }

    @Override
    public GasMekanismCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new GasMekanismCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super GasStack> createContentWidget() {
        return new GasStackWidget();
    }

    @Override
    public GasStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new GasStack(GasRegistry.getGas(jsonElement.getAsJsonObject().get("gas").getAsString()), jsonElement.getAsJsonObject().get("amount").getAsInt());
    }

    @Override
    public JsonElement serialize(GasStack gasStack, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("gas", gasStack.getGas().getName());
        jsonObj.addProperty("amount", gasStack.amount);
        return jsonObj;
    }

    public static class GasMekanismCapabilityProxy extends CapabilityProxy<GasStack> {

        public GasMekanismCapabilityProxy(TileEntity tileEntity) {
            super(GasMekanismCapability.CAP, tileEntity);
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

    }
}
