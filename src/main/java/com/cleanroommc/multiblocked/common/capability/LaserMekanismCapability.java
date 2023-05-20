package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.LaserCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.MekanismBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author WaitingIdly
 */
public class LaserMekanismCapability extends MultiblockCapability<Double> {
    public static final LaserMekanismCapability CAP = new LaserMekanismCapability();

    public LaserMekanismCapability() {
        super("mek_laser", new Color(0x805080).getRGB());
    }

    @Override
    public Double defaultContent() {
        return 5.0;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof ILaserReceptor;
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public Double copyInnerByModifier(Double content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    @Override
    public CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new MekanismLaserProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Double> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("LZ", color)).setUnit("Mekanism Laser");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {
                new BlockInfo(MekanismBlocks.MachineBlock2),
                new BlockInfo(Blocks.BONE_BLOCK)
        };
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
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new LaserCapabilityTrait();
    }

    public static class MekanismLaserProxy extends CapabilityProxy<Double> {
        public MekanismLaserProxy(TileEntity tileEntity) {
            super(LaserMekanismCapability.CAP, tileEntity);
        }

        public ILaserReceptor getCapability() {
            return (ILaserReceptor)getTileEntity();
        }

        @Override
        protected List<Double> handleRecipeInner(IO io, Recipe recipe, List<Double> left, @Nullable String slotName, boolean simulate) {
            ILaserReceptor capability = getCapability();
            if (capability == null) return left;
            double sum = left.stream().reduce(0.0, Double::sum);
            if (io == IO.IN) {
                if (!simulate) {
                    capability.receiveLaserEnergy(-sum, EnumFacing.DOWN);
                }
            } else if (io == IO.OUT) {
                if (!simulate) {
                    capability.receiveLaserEnergy(-sum, EnumFacing.DOWN);
                }
            }
            return null;
        }

        @Override
        protected boolean hasInnerChanged() {
            ILaserReceptor capability = getCapability();
            return capability != null;
        }
    }
}
