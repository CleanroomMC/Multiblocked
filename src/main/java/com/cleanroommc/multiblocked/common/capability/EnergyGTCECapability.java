package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.EnergyCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.cleanroommc.multiblocked.common.capability.widget.TieredNumberContentWidget;
import com.google.gson.*;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.integration.jei.utils.JEIHelpers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyGTCECapability extends MultiblockCapability<Long> {
    public static final EnergyGTCECapability CAP = new EnergyGTCECapability();

    public EnergyGTCECapability() {
        super("gtce_energy", new Color(0xF3C225).getRGB());
    }

    @Override
    public Long defaultContent() {
        return 256L;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, tileEntity).isEmpty();
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public Long copyInnerByModifier(Long content, ContentModifier modifier) {
        return ((long) modifier.apply(content));
    }

    @Override
    public EnergyCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new EnergyCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Long> createContentWidget() {
        if (isCEu()) {
            return new TieredNumberContentWidget()
                    .setTierFunction(EU -> String.format(" (%s)", GTValues.VNF[JEIHelpers.getMinTierForVoltage(EU)] + TextFormatting.RESET))
                    .setContentTexture(new TextTexture("EU", color))
                    .setUnit("EU");
        } else {
            return new NumberContentWidget().setContentTexture(new TextTexture("EU", color)).setUnit("eu");
        }
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new EnergyCapabilityTrait();
    }

    public boolean isCEu() {
        String version = Loader.instance().getIndexedModList().get("gregtech").getVersion();
        return Integer.parseInt(version.split("\\.")[0]) >= 2;
    }

    @Override
    public BlockInfo[] getCandidates() {
        List<BlockInfo> list = new ArrayList<>();
        if (isCEu()) {
            IBlockState blockState = GregTechAPI.MACHINE.getDefaultState();
            for (MetaTileEntityEnergyHatch energyInputHatch : MetaTileEntities.ENERGY_INPUT_HATCH) {
                if (energyInputHatch == null) continue;
                MetaTileEntityHolder holder = new MetaTileEntityHolder();
                ItemStack itemStack = energyInputHatch.getStackForm();
                holder.setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(itemStack.getItemDamage()));
                list.add(new BlockInfo(blockState, holder, itemStack));
            }
            for (MetaTileEntityEnergyHatch energyOutputHatch : MetaTileEntities.ENERGY_OUTPUT_HATCH) {
                if (energyOutputHatch == null) continue;
                MetaTileEntityHolder holder = new MetaTileEntityHolder();
                ItemStack itemStack = energyOutputHatch.getStackForm();
                holder.setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(itemStack.getItemDamage()));
                list.add(new BlockInfo(blockState, holder, itemStack));
            }
        } else { // time to get rid of the gtce
        }
        return list.toArray(new BlockInfo[0]);
    }

    @Override
    public Long deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsLong();
    }

    @Override
    public JsonElement serialize(Long integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    public static class EnergyCapabilityProxy extends CapCapabilityProxy<IEnergyContainer, Long> {

        public EnergyCapabilityProxy(TileEntity tileEntity) {
            super(EnergyGTCECapability.CAP, tileEntity, GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        }

        @Override
        protected List<Long> handleRecipeInner(IO io, Recipe recipe, List<Long> left, boolean simulate) {
            IEnergyContainer capability = getCapability();
            if (capability == null) return left;
            long sum = left.stream().reduce(0L, Long::sum);
            if (io == IO.IN) {
                if (!simulate) {
                    capability.addEnergy(-Math.min(capability.getEnergyStored(), sum));
                }
                sum = sum - capability.getEnergyStored();
            } else if (io == IO.OUT) {
                long canInput = capability.getEnergyCapacity() - capability.getEnergyStored();
                if (!simulate) {
                    capability.addEnergy(Math.min(canInput, sum));
                }
                sum = sum - canInput;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        long stored = -1;

        @Override
        protected boolean hasInnerChanged() {
            IEnergyContainer capability = getCapability();
            if (capability == null) return false;
            if (capability.getEnergyStored() == stored) {
                return false;
            }
            stored = capability.getEnergyStored();
            return true;
        }
    }
}
