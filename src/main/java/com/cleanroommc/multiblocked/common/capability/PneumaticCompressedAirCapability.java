package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.PneumaticMachineTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author youyihj
 */
public class PneumaticCompressedAirCapability extends PneumaticBaseCapability<Integer> {
    public static final PneumaticCompressedAirCapability CAP = new PneumaticCompressedAirCapability();

    protected PneumaticCompressedAirCapability() {
        super("pneumatic_compress_air", new Color(0x444444).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 1000;
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Integer> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new Proxy(tileEntity);
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
        return new NumberContentWidget().setContentTexture(new TextTexture("CA", color)).setUnit("Compressed Air");
    }

    @Override
    public CapabilityTrait createTrait() {
        return new PneumaticMachineTrait(this);
    }

    private static class Proxy extends PneumaticBaseCapability.Proxy<Integer> {
        public Proxy(TileEntity tileEntity) {
            super(CAP, tileEntity);
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IAirHandler handler = getAirHandler();
            if (!simulate) {
                handler.addAir(left.stream().reduce(0, Integer::sum));
            }
            return null;
        }
    }
}
