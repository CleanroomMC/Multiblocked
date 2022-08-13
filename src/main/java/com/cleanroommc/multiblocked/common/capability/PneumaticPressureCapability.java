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
import com.cleanroommc.multiblocked.common.capability.trait.PneumaticMachineTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author youyihj
 */
public class PneumaticPressureCapability extends MultiblockCapability<Float> {
    public static final PneumaticPressureCapability CAP = new PneumaticPressureCapability();

    protected PneumaticPressureCapability() {
        super("pneumatic_pressure", new Color(0xFF3C00).getRGB());
    }

    @Override
    public Float defaultContent() {
        return 2.0f;
    }

    @Override
    public Float copyInner(Float content) {
        return content;
    }

    @Override
    public Float copyInnerByModifier(Float content, ContentModifier modifier) {
        return (float) modifier.apply(content);
    }

    @Override
    public CapabilityProxy<? extends Float> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new Proxy(tileEntity);
    }

    @Override
    public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsFloat();
    }

    @Override
    public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IPneumaticMachine;
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {new BlockInfo(Blockss.PRESSURE_CHAMBER_WALL)};
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public ContentWidget<? super Float> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("P", color)).setUnit("bar");
    }

    @Override
    public CapabilityTrait createTrait() {
        return new PneumaticMachineTrait();
    }

    private static class Proxy extends CapabilityProxy<Float> {
        public Proxy(TileEntity tileEntity) {
            super(CAP, tileEntity);
        }

        public IAirHandler getAirHandler() {
            IPneumaticMachine machine = (IPneumaticMachine) getTileEntity();
            if (machine != null) {
                for (EnumFacing value : EnumFacing.values()) {
                    IAirHandler airHandler = machine.getAirHandler(value);
                    if (airHandler != null) return airHandler;
                }
            }
            return null;
        }

        int air;
        int volume;

        @Override
        protected boolean hasInnerChanged() {
            IAirHandler airHandler = getAirHandler();
            if (airHandler == null) return false;
            if (airHandler.getAir() == air && airHandler.getVolume() == volume) {
                return false;
            }
            air = airHandler.getAir();
            volume = airHandler.getVolume();
            return true;
        }

        @Override
        protected List<Float> handleRecipeInner(IO io, Recipe recipe, List<Float> left, boolean simulate) {
            IAirHandler handler = getAirHandler();
            float sum = left.stream().reduce(0.0f, Float::sum);
            int consumeAir = (int) (sum * 50);
            if (handler != null && io == IO.IN) {
                int air = handler.getAir();
                if (Math.signum(air) == Math.signum(consumeAir) && Math.abs(air) >= Math.abs(consumeAir) && Math.abs(handler.getPressure()) >= Math.abs(sum)) {
                    if (!simulate) {
                        handler.addAir(-consumeAir);
                    }
                    return null;
                }
            }
            return left;
        }
    }
}

