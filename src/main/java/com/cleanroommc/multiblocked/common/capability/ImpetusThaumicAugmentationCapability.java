package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.ImpetusCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
public class ImpetusThaumicAugmentationCapability extends MultiblockCapability<Long> {
    public static final ImpetusThaumicAugmentationCapability CAP = new ImpetusThaumicAugmentationCapability();

    public ImpetusThaumicAugmentationCapability() {
        super("ta_impetus", new Color(0x805080).getRGB());
    }

    @Override
    public Long defaultContent() {
        return 5L;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, tileEntity).isEmpty();
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Long> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ImpetusCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Long> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("IM", color)).setUnit("");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {new BlockInfo(TABlocks.IMPETUS_MATRIX)};
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
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new ImpetusCapabilityTrait();
    }

    public static class ImpetusCapabilityProxy extends CapCapabilityProxy<IImpetusStorage, Long> {
        public ImpetusCapabilityProxy(TileEntity tileEntity) {
            super(ImpetusThaumicAugmentationCapability.CAP, tileEntity, CapabilityImpetusStorage.IMPETUS_STORAGE);
        }

        @Override
        protected List<Long> handleRecipeInner(IO io, Recipe recipe, List<Long> left, boolean simulate) {
            IImpetusStorage capability = getCapability();
            if (capability == null) return left;
            long sum = left.stream().reduce(0L, Long::sum);
            if (io == IO.IN) {
                sum = sum - capability.extractEnergy(sum, simulate);
            } else if (io == IO.OUT) {
                sum = sum - capability.receiveEnergy(sum, simulate);
            }
            return sum > 0 ? Collections.singletonList(sum) : null;
        }

        long stored = -1L;
        boolean canReceive = false;
        boolean canExtract = false;

        @Override
        protected boolean hasInnerChanged() {
            IImpetusStorage capability = getCapability();
            if (capability == null) return false;
            if (capability.getEnergyStored() == stored && capability.canExtract() == canExtract && capability.canReceive() == canReceive) {
                return false;
            }
            stored = capability.getEnergyStored();
            canExtract = capability.canExtract();
            canReceive = capability.canReceive();
            return true;
        }
    }
}
