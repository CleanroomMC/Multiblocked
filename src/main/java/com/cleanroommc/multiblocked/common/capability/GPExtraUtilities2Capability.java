package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.trait.GPPlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class GPExtraUtilities2Capability extends MultiblockCapability<Float> {
    public static final GPExtraUtilities2Capability CAP = new GPExtraUtilities2Capability();

    private GPExtraUtilities2Capability() {
        super("exu2_gp", new Color(0xD02CAB).getRGB());
    }

    @Override
    public Float defaultContent() {
        return 15f;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof ComponentTileEntity && ((ComponentTileEntity<?>) tileEntity).hasTrait(GPExtraUtilities2Capability.CAP);
    }

    @Override
    public Float copyInner(Float content) {
        return content;
    }

    @Override
    public GPExtraUtilities2CapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new GPExtraUtilities2CapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Float> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("GP", color)).setUnit("GP");
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new GPPlayerCapabilityTrait();
    }

    @Override
    public BlockInfo[] getCandidates() {
        return MbdComponents.DEFINITION_REGISTRY
                .values()
                .stream()
                .filter(definition -> definition.traits.has(CAP.name))
                .map(definition -> BlockInfo.fromBlockState(MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location).getDefaultState()))
                .toArray(BlockInfo[]::new);
    }

    @Override
    public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsFloat();
    }

    @Override
    public JsonElement serialize(Float aFloat, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(aFloat);
    }

    public static class GPExtraUtilities2CapabilityProxy extends CapabilityProxy<Float> {

        public GPExtraUtilities2CapabilityProxy(TileEntity tileEntity) {
            super(GPExtraUtilities2Capability.CAP, tileEntity);
        }

        @Override
        protected List<Float> handleRecipeInner(IO io, Recipe recipe, List<Float> left, boolean simulate) {
            TileEntity te = getTileEntity();
            if (te instanceof ComponentTileEntity && ((ComponentTileEntity<?>) te).hasTrait(GPExtraUtilities2Capability.CAP)) {
                CapabilityTrait trait = ((ComponentTileEntity<?>) te).getTrait(GPExtraUtilities2Capability.CAP);
                if (trait instanceof GPPlayerCapabilityTrait) {
                    float sum = left.stream().reduce(0f, Float::sum);
                    sum = ((GPPlayerCapabilityTrait) trait).updatePower(io == IO.IN ? sum : -sum, 2, simulate);
                    return sum <= 0 ? null : Collections.singletonList(sum);
                }
            }
            return left;
        }

        float lastPower;
        @Override
        protected boolean hasInnerChanged() {
            TileEntity te = getTileEntity();
            if (te instanceof ComponentTileEntity && ((ComponentTileEntity<?>) te).hasTrait(GPExtraUtilities2Capability.CAP)) {
                CapabilityTrait trait = ((ComponentTileEntity<?>) te).getTrait(GPExtraUtilities2Capability.CAP);
                if (trait instanceof GPPlayerCapabilityTrait) {
                    if (lastPower != ((GPPlayerCapabilityTrait) trait).getPowerCreated()) {
                        lastPower = ((GPPlayerCapabilityTrait) trait).getPowerCreated();
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
