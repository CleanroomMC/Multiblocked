package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import lykrast.prodigytech.common.capability.CapabilityHotAir;
import lykrast.prodigytech.common.capability.IHotAir;
import lykrast.prodigytech.common.init.ModBlocks;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.List;

public class HotAirProdigyCapability extends MultiblockCapability<Integer> {
    public static final HotAirProdigyCapability CAP = new HotAirProdigyCapability();

    private HotAirProdigyCapability() {
        super("prodigy_hotair", new Color(0xD91F06).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 0;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(CapabilityHotAir.HOT_AIR, tileEntity).isEmpty();
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyInnerByModifier(Integer content, ContentModifier modifier) {
        return ((int) modifier.apply(content));
    }

    @Override
    public HotAirProdigyCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new HotAirProdigyCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("HA", color)).setUnit("°C");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {
                BlockInfo.fromBlockState(ModBlocks.aeroheaterMagmatic.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.aeroheaterEnergion.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.aeroheaterSolid.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.aeroheaterTartaric.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.aeroheaterCapacitor.getDefaultState()),
        };
    }

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer aInteger, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(aInteger);
    }

    public static class HotAirProdigyCapabilityProxy extends CapCapabilityProxy<IHotAir, Integer> {

        public HotAirProdigyCapabilityProxy(TileEntity tileEntity) {
            super(HotAirProdigyCapability.CAP, tileEntity, CapabilityHotAir.HOT_AIR);
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IHotAir capability = getCapability();
            if (capability == null || capability.getOutAirTemperature() < left.get(0)) return left;
            return null;
        }

        Integer lastTemp = Integer.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            IHotAir capability = getCapability();
            if (capability == null || capability.getOutAirTemperature() <= 0) return false;
            if (lastTemp == capability.getOutAirTemperature()) return false;
            lastTemp = capability.getOutAirTemperature();
            return true;
        }
    }
}
