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

import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.handler.RegistryHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MechPowerCapability extends MultiblockCapability<Double> {
    public static final MechPowerCapability CAP = new MechPowerCapability();

    private MechPowerCapability() {
        super("mm_mech_power", new Color(0xA2A2E9).getRGB());
    }

    @Override
    public Double defaultContent() {
        return 0.0d;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, EnumFacing.byIndex(tileEntity.getBlockMetadata()).getOpposite());
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public Double copyInnerByModifier(Double content, ContentModifier modifier) {
        return ((double) modifier.apply(content));
    }

    @Override
    public MechPowerCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new MechPowerCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Double> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("Mechanical Power", color)).setUnit("MP");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {
                BlockInfo.fromBlockState(RegistryHandler.GEARBOX_FRAME.getDefaultState()),
                BlockInfo.fromBlockState(RegistryHandler.MERGEBOX_FRAME.getDefaultState()),
        };
    }

    @Override
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsDouble();
    }

    @Override
    public JsonElement serialize(Double aDouble, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(aDouble);
    }

    public static class MechPowerCapabilityProxy extends CapCapabilityProxy<IMechCapability, Double> {
        public MechPowerCapabilityProxy(TileEntity tileEntity) {
            super(MechPowerCapability.CAP, tileEntity, MysticalMechanicsAPI.MECH_CAPABILITY);
        }

        public EnumFacing getTargetFacingDirection() {
            return EnumFacing.byIndex(getTileEntity().getBlockMetadata()).getOpposite();
        }

        @Override
        protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, @Nullable String slotName, boolean simulate) {
            IMechCapability capability = getCapability(slotName);
            if (capability == null || capability.getPower(getTargetFacingDirection()) < left.get(0)) {
                return left;
            }
            return null;
        }

        double lastPower = Double.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            IMechCapability capability = getCapability(null);

            if (capability == null || capability.getPower(getTargetFacingDirection()) == 0) {
                return false;
            }
            if (lastPower == capability.getPower(getTargetFacingDirection())) {
                return false;
            }

            lastPower = capability.getPower(getTargetFacingDirection());
            return true;
        }
    }
}
