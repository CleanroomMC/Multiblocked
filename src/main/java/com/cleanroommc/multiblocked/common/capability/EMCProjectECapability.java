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
import com.cleanroommc.multiblocked.common.capability.trait.EMCPlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class EMCProjectECapability extends MultiblockCapability<Long> {

    public static final EMCProjectECapability CAP = new EMCProjectECapability();

    protected EMCProjectECapability() {
        super("projecte_emc", new Color(0xAC2D5E).getRGB());
    }

    @Override
    public Long defaultContent() {
        return 1L;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof ComponentTileEntity && ((ComponentTileEntity<?>) tileEntity).hasTrait(EMCProjectECapability.CAP);
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Long> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new EMCProjectECapabilityProxy(tileEntity);
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
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsLong();
    }

    @Override
    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public ContentWidget<? super Long> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("EMC", color)).setUnit("EMC");
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new EMCPlayerCapabilityTrait();
    }

    public static class EMCProjectECapabilityProxy extends CapabilityProxy<Long> {

        public EMCProjectECapabilityProxy(TileEntity tileEntity) {
            super(EMCProjectECapability.CAP, tileEntity);
        }

        public EMCPlayerCapabilityTrait getTrait() {
            TileEntity te = getTileEntity();
            if (te instanceof ComponentTileEntity && ((ComponentTileEntity<?>) te).hasTrait(EMCProjectECapability.CAP)) {
                CapabilityTrait trait = ((ComponentTileEntity<?>) te).getTrait(EMCProjectECapability.CAP);
                if (trait instanceof EMCPlayerCapabilityTrait) {
                    return (EMCPlayerCapabilityTrait) trait;
                }
            }
            return null;
        }

        @Override
        protected List<Long> handleRecipeInner(IO io, Recipe recipe, List<Long> left, boolean simulate) {
            EMCPlayerCapabilityTrait trait = getTrait();
            long sum = left.stream().reduce(0L, Long::sum);
            sum = trait.updateEMC(io == IO.IN ? -sum : sum, simulate);
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        long lastEMC = Long.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            EMCPlayerCapabilityTrait trait = getTrait();
            if (trait == null) return false;
            IKnowledgeProvider capability = trait.getCapability();
            if (capability == null) return false;
            if (lastEMC == capability.getEmc()) return false;
            lastEMC = trait.getCapability().getEmc();
            return true;
        }
    }
}
