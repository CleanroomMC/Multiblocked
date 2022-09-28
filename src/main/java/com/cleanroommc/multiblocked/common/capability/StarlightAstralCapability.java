package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.StarlightCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.StarlightWidget;
import com.cleanroommc.multiblocked.common.recipe.content.Starlight;
import com.google.gson.*;
import hellfirepvp.astralsorcery.common.auxiliary.link.ILinkableTile;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author youyihj
 */
public class StarlightAstralCapability extends MultiblockCapability<Starlight> {
    public static final StarlightAstralCapability CAP = new StarlightAstralCapability();

    protected StarlightAstralCapability() {
        super("starlight_as", new Color(0xACB0B1).getRGB());
        TransmissionClassRegistry.register(new StarlightCapabilityTrait.MultiblockedTransmissionProvider());
    }

    @Override
    public Starlight defaultContent() {
        return new Starlight(800, null);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof StarlightAstralCapability.ILinkableStarlightReceiver;
    }

    @Override
    public Starlight copyInner(Starlight content) {
        return content.copy();
    }

    @Override
    public Starlight copyInnerByModifier(Starlight content, ContentModifier modifier) {
        return new Starlight(((int) modifier.apply(content.getValue())), content.getConstellation());
    }

    @Override
    public CapabilityProxy<? extends Starlight> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new StarlightAstralProxy(tileEntity);
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new StarlightCapabilityTrait();
    }

    @Override
    public ContentWidget<? super Starlight> createContentWidget() {
        return new StarlightWidget();
    }

    @Override
    public Starlight deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int starlight = JsonUtils.getInt(jsonObject, "starlight");
        JsonElement constellationJson = jsonObject.get("constellation");
        IConstellation constellation = constellationJson == null || constellationJson.isJsonNull() ? null : ConstellationRegistry.getConstellationByName(constellationJson.getAsString());
        return new Starlight(starlight, constellation);
    }

    @Override
    public JsonElement serialize(Starlight src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("starlight", src.getValue());
        jsonObject.addProperty("constellation", src.getConstellation() == null ? null : src.getConstellation().getUnlocalizedName());
        return jsonObject;
    }

    public static class StarlightAstralProxy extends CapabilityProxy<Starlight> {

        public StarlightAstralProxy(TileEntity tileEntity) {
            super(StarlightAstralCapability.CAP, tileEntity);
        }

        public ILinkableStarlightReceiver getCapability() {
            return (ILinkableStarlightReceiver) getTileEntity();
        }

        public boolean matchConstellation(Starlight starlight) {
            IConstellation constellation = starlight.getConstellation();
            return constellation == null || constellation.equals(getCapability().getFocusedConstellation());
        }

        @Override
        protected List<Starlight> handleRecipeInner(IO io, Recipe recipe, List<Starlight> left, boolean simulate) {
            ILinkableStarlightReceiver tileEntity = (ILinkableStarlightReceiver) getTileEntity();
            int sum = left.stream()
                    .filter(this::matchConstellation)
                    .mapToInt(Starlight::getValue)
                    .sum();
            boolean accepted = false;
            switch (io) {
                case IN:
                    if (tileEntity.getStarlightStored() >= sum) {
                        if (!simulate) {
                            tileEntity.setStarlightStored(tileEntity.getStarlightStored() - sum);
                        }
                        accepted = true;
                    }
                    break;
                case OUT:
                    if (tileEntity.getStarlightStored() + sum <= tileEntity.getStarlightCapacity()) {
                        if (!simulate) {
                            tileEntity.setStarlightStored(tileEntity.getStarlightStored() + sum);
                        }
                        accepted = true;
                    }
                    break;
            }
            if (accepted) {
                left.removeIf(this::matchConstellation);
            }
            return left.isEmpty() ? null : left;
        }

        IConstellation lastConstellation;
        int lastStarlight;

        @Override
        protected boolean hasInnerChanged() {
            ILinkableStarlightReceiver capability = getCapability();
            if (capability == null) return false;
            if (lastStarlight != capability.getStarlightStored() || lastConstellation != capability.getFocusedConstellation()) {
                lastStarlight = capability.getStarlightStored();
                lastConstellation = capability.getFocusedConstellation();
                return true;
            }
            return false;
        }
    }

    public interface ILinkableStarlightReceiver extends ILinkableTile, IStarlightReceiver {
        int getStarlightStored();

        void setStarlightStored(int starlight);

        int getStarlightCapacity();

        IConstellation getFocusedConstellation();

        void setFocusedConstellation(IConstellation constellation);
    }
}
