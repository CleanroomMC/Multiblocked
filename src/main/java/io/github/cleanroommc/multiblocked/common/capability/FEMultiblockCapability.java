package io.github.cleanroommc.multiblocked.common.capability;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import io.github.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class FEMultiblockCapability extends MultiblockCapability<Integer> {

    public FEMultiblockCapability() {
        super("forge_energy", new Color(0xCB0000).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        IEnergyStorage capability = tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
        return capability != null && (io == IO.IN && capability.canExtract() ||
                        io == IO.OUT && capability.canReceive() ||
                        io == IO.BOTH && capability.canReceive() && capability.canExtract());
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
        return new NumberContentWidget().setContentTexture(new ColorRectTexture(this.color)).setUnit("FE");
    }

    @Override
    public IBlockState[] getCandidates(IO io) {
        return scanForCandidates(io);
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
            return getTileEntity().getCapability(CapabilityEnergy.ENERGY, null);
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
