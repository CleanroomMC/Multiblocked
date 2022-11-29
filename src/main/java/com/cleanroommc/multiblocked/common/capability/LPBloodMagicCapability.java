package com.cleanroommc.multiblocked.common.capability;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.trait.LPPlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class LPBloodMagicCapability extends MultiblockCapability<Integer> {

    public static final LPBloodMagicCapability CAP = new LPBloodMagicCapability();

    private LPBloodMagicCapability() {
        super("bg_lp", new Color(0xF34C4E).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 1;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof ComponentTileEntity && ((ComponentTileEntity<?>) tileEntity).hasTrait(LPBloodMagicCapability.CAP);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyInnerByModifier(Integer content, ContentModifier modifier) {
        return (int) modifier.apply(content);
    }

    @Override
    public CapabilityProxy<? extends Integer> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new LPBloodMagicCapabilityProxy(tileEntity);
    }

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("LP", color)).setUnit("LP");
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new LPPlayerCapabilityTrait();
    }

    public static class LPBloodMagicCapabilityProxy extends CapabilityProxy<Integer> {

        public LPBloodMagicCapabilityProxy(TileEntity tileEntity) {
            super(LPBloodMagicCapability.CAP, tileEntity);
        }

        public LPPlayerCapabilityTrait getTrait() {
            TileEntity te = getTileEntity();
            if (te instanceof ComponentTileEntity && ((ComponentTileEntity<?>) te).hasTrait(LPBloodMagicCapability.CAP)) {
                CapabilityTrait trait = ((ComponentTileEntity<?>) te).getTrait(LPBloodMagicCapability.CAP);
                if (trait instanceof LPPlayerCapabilityTrait) {
                    return (LPPlayerCapabilityTrait) trait;
                }
            }
            return null;
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
            LPPlayerCapabilityTrait trait = getTrait();
            if (trait == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            sum = trait.updateLP(io == IO.IN ? -sum : sum, simulate);
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        int lastLP = Integer.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            LPPlayerCapabilityTrait trait = getTrait();
            if (trait == null) return false;
            SoulNetwork capability = trait.getCapability();
            if (capability == null) return false;
            if (lastLP == capability.getCurrentEssence()) return false;
            lastLP = capability.getCurrentEssence();
            return true;
        }
    }
}
