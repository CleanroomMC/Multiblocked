package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import lykrast.prodigytech.common.capability.CapabilityHotAir;
import lykrast.prodigytech.common.capability.IHotAir;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
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
    public HotAirProdigyCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new HotAirProdigyCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("HA", color)).setUnit("HotAir");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[0];
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
            if (capability == null || capability.getOutAirTemperature() <= left.get(0)) return left;
            return null;
        }
    }
}
