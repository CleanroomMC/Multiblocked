package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;
import sblectric.lightningcraft.init.LCBlocks;
import sblectric.lightningcraft.tiles.TileEntityLightningCell;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class LEMultiblockCapability extends MultiblockCapability<Double> {

    public static final LEMultiblockCapability CAP = new LEMultiblockCapability();

    private LEMultiblockCapability() {
        super("lightningcraft_le", new Color(0xE4D00A).getRGB());
    }

    @Override
    public Double defaultContent() {
        return 100d;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof TileEntityLightningCell;
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new LECapabilityProxy(this, tileEntity);
    }

    @Override
    public ContentWidget<? super Double> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("LE", color)).setUnit("LE");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return LCBlocks.lightningCell.getBlockState().getValidStates().stream().map(BlockInfo::new).toArray(BlockInfo[]::new);
    }

    @Override
    public Double deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsDouble();
    }

    @Override
    public JsonElement serialize(Double aDouble, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(aDouble);
    }

    public static class LECapabilityProxy extends CapabilityProxy<Double> {

        public LECapabilityProxy(MultiblockCapability<? super Double> capability, TileEntity tileEntity) {
            super(capability, tileEntity);
        }

        @Override
        protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, boolean simulate) {
            TileEntityLightningCell tile = (TileEntityLightningCell) getTileEntity();
            if (tile == null) return left;
            double sum = left.stream().reduce(0d, Double::sum);
            if (io == IO.IN) {
                double in = Math.min(tile.storedPower, sum);
                if (!simulate) {
                    tile.storedPower -= in;
                }
                sum -= in;
            } else if (io == IO.OUT) {
                double out = Math.min(tile.maxPower - tile.storedPower, sum);
                if (!simulate) {
                    tile.storedPower += out;
                }
                sum -= out;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        double storedPower;

        @Override
        protected boolean hasInnerChanged() {
            TileEntityLightningCell tile = (TileEntityLightningCell) getTileEntity();
            if (tile == null || tile.storedPower == storedPower) {
                return false;
            }
            storedPower = tile.storedPower;
            return true;
        }

    }

}
