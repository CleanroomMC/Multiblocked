package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class EmberEmbersCapability extends MultiblockCapability<Double> {

    public static final EmberEmbersCapability CAP = new EmberEmbersCapability();

    protected EmberEmbersCapability() {
        super("ember", new Color(0xFFE192).getRGB());
    }

    @Override
    public Double defaultContent() {
        return 0D;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(EmbersCapabilities.EMBER_CAPABILITY, tileEntity).isEmpty();
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new EmberEmbersCapabilityProxy(tileEntity);
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[0];
    }

    @Override
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsDouble();
    }

    @Override
    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public ContentWidget<? super Double> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("EB", color)).setUnit("Embers");
    }

    public static class EmberEmbersCapabilityProxy extends CapCapabilityProxy<IEmberCapability, Double> {

        public EmberEmbersCapabilityProxy(TileEntity tileEntity) {
            super(EmberEmbersCapability.CAP, tileEntity, EmbersCapabilities.EMBER_CAPABILITY);
        }

        @Override
        protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, boolean simulate) {
            IEmberCapability capability = getCapability();
            double ember = capability.getEmber();
            double emberCapacity = capability.getEmberCapacity();

            double sum = left.stream().reduce(0D, Double::sum);
            if (io == IO.IN) {
                double cost = Math.min(ember, sum);
                if (!simulate) {
                    capability.setEmber(ember - cost);
                }
                sum -= cost;
            } else if (io == IO.OUT) {
                if (ember >= emberCapacity) {
                    return left;
                }
                double canInput = emberCapacity - ember;
                if (!simulate) {
                    double stored = Math.min(canInput, sum);

                    capability.setEmber(Math.min(emberCapacity, ember + stored));
                }
                sum -= canInput;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        double lastEmber = Integer.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            IEmberCapability capability = getCapability();
            if (capability == null) return false;
            if (lastEmber == capability.getEmber()) return false;
            lastEmber = capability.getEmber();
            return true;
        }
    }
}
