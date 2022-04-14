package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.GasStackWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.MekanismBlocks;
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
    public GasStack defaultContent() {
        return new GasStack(GasRegistry.getGas(0), 1000);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(Capabilities.GAS_HANDLER_CAPABILITY, tileEntity).isEmpty();
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
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {BlockInfo.fromBlockState(MekanismBlocks.GasTank.getDefaultState())};
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

    public static class GasMekanismCapabilityProxy extends CapCapabilityProxy<IGasHandler, GasStack> {

        public GasMekanismCapabilityProxy(TileEntity tileEntity) {
            super(GasMekanismCapability.CAP, tileEntity, Capabilities.GAS_HANDLER_CAPABILITY);
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
                            gasStack.amount -= capability.receiveGas(facing, gasStack.copy(), !simulate);
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

        GasStack[] lastStacks = new GasStack[0];
        int[] lastStoreds = new int[0];
        int[] lastMaxs = new int[0];

        @Override
        protected boolean hasInnerChanged() {
            IGasHandler capability = getCapability();
            if (capability == null) return false;
            GasTankInfo[] tanks = capability.getTankInfo();
            boolean same = true;
            if (lastStacks.length == tanks.length) {
                for (int i = 0; i < tanks.length; i++) {
                    GasTankInfo tank = tanks[i];
                    GasStack content = tank.getGas();
                    GasStack lastContent = lastStacks[i];
                    if (content == null) {
                        if (lastContent != null) {
                            same = false;
                            break;
                        }
                    } else {
                        if (lastContent == null) {
                            same = false;
                            break;
                        } else if (!content.isGasEqual(lastContent)) {
                            same = false;
                            break;
                        }
                    }
                    int cap = tank.getStored();
                    int lastCap = lastStoreds[i];
                    if (cap != lastCap) {
                        same = false;
                        break;
                    }
                    int max = tank.getMaxGas();
                    int lastMax = lastMaxs[i];
                    if (max != lastMax) {
                        same = false;
                        break;
                    }
                }
            } else {
                same = false;
            }

            if (same) {
                return false;
            }
            lastStacks = new GasStack[tanks.length];
            lastStoreds = new int[tanks.length];
            lastMaxs = new int[tanks.length];
            for (int i = 0; i < tanks.length; i++) {
                GasTankInfo tank = tanks[i];
                GasStack gas = tank.getGas();
                lastStacks[i] = gas == null ? null : gas.copy();
                lastStoreds[i] = tank.getStored();
                lastMaxs[i] = tank.getMaxGas();
            }
            return true;
        }
    }
}
