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
import com.cleanroommc.multiblocked.common.capability.trait.ManaCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ManaBotaniaCapability extends MultiblockCapability<Integer> {
    public static final ManaBotaniaCapability CAP = new ManaBotaniaCapability();

    private ManaBotaniaCapability() {
        super("bot_mana", new Color(0x06D2D9).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 200;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IManaReceiver;
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
    public ManaBotainaCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ManaBotainaCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("MN", color)).setUnit("Mana");
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[]{
                BlockInfo.fromBlockState(ModBlocks.pool.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.spreader.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.manaVoid.getDefaultState()),
                BlockInfo.fromBlockState(ModBlocks.terraPlate.getDefaultState())
        };
    }

    @Override
    public Integer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new ManaCapabilityTrait();
    }

    public static class ManaBotainaCapabilityProxy extends CapabilityProxy<Integer> {

        public ManaBotainaCapabilityProxy(TileEntity tileEntity) {
            super(ManaBotaniaCapability.CAP, tileEntity);
        }

        public IManaReceiver getCapability() {
            return (IManaReceiver)getTileEntity();
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
            IManaReceiver capability = getCapability();
            if (capability == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                int cost = Math.min(capability.getCurrentMana(), sum);
                if (!simulate) {
                    capability.recieveMana(-cost);
                }
                sum = sum - cost;
            } else if (io == IO.OUT) {
                if (capability.isFull()) {
                    return left;
                }
                if (!simulate) {
                    capability.recieveMana(sum);
                }
                return null;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        int lastMana = -1;

        @Override
        protected boolean hasInnerChanged() {
            IManaReceiver capability = getCapability();
            if (capability == null) return false;
            if (lastMana == capability.getCurrentMana()) return false;
            lastMana = capability.getCurrentMana();
            return true;
        }
    }
}
