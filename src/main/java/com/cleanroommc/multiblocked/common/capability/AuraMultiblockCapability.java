package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.MbdConfig;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
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
import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.List;

public class AuraMultiblockCapability extends MultiblockCapability<Integer> {

    public static final AuraMultiblockCapability CAP = new AuraMultiblockCapability();

    private AuraMultiblockCapability() {
        super("natures_aura", new Color(0x95EF95).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 200;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        // The specific storage of aura should be in the chunk and not inside te
        return true;
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
        return new ManaBotainaCapabilityProxy(tileEntity);
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[]{
                BlockInfo.fromBlockState(Blocks.FURNACE.getDefaultState()),
                BlockInfo.fromBlockState(Blocks.CHEST.getDefaultState()),
                BlockInfo.fromBlockState(Blocks.HOPPER.getDefaultState()),
                BlockInfo.fromBlockState(Blocks.ENDER_CHEST.getDefaultState())
        };
    }

    @Override
    public Integer deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return jsonElement.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer integer, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(integer);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("NA", color)).setUnit("NaturesAura");
    }

    public static class ManaBotainaCapabilityProxy extends CapabilityProxy<Integer> {

        public ManaBotainaCapabilityProxy(TileEntity tileEntity) {
            super(AuraMultiblockCapability.CAP, tileEntity);
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
            World world = getTileEntity().getWorld();
            BlockPos pos = getTileEntity().getPos();

            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                if (!simulate) {
                    BlockPos spot = IAuraChunk.getHighestSpot(world, pos, MbdConfig.naturesAura.radius, pos);
                    IAuraChunk.getAuraChunk(world, spot).drainAura(pos, sum);
                }
            } else if (io == IO.OUT) {
                if (!simulate) {
                    BlockPos spot = IAuraChunk.getLowestSpot(world, pos, MbdConfig.naturesAura.radius, pos);
                    IAuraChunk.getAuraChunk(world, spot).storeAura(pos, sum);
                }
            }
            return null;
        }

        int lastMana = Integer.MIN_VALUE;

        @Override
        protected boolean hasInnerChanged() {
            int auraInArea = IAuraChunk.getAuraInArea(getTileEntity().getWorld(),
                    getTileEntity().getPos(), MbdConfig.naturesAura.radius);
            if (lastMana == auraInArea) return false;
            lastMana = auraInArea;
            return true;
        }
    }
}
