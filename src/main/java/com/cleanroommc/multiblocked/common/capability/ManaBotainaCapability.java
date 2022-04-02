package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManaBotainaCapability extends MultiblockCapability<Integer> {
    public static final ManaBotainaCapability CAP = new ManaBotainaCapability();

    private ManaBotainaCapability() {
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
    public ManaBotainaCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ManaBotainaCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new ItemStackTexture(
                Arrays.stream(getCandidates())
                        .map(state -> new ItemStack(
                                Item.getItemFromBlock(state.getBlock()), 1,
                                state.getBlock().damageDropped(state)))
                        .toArray(ItemStack[]::new))).setUnit("mana");
    }

    @Override
    public Integer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    public static class ManaBotainaCapabilityProxy extends CapabilityProxy<Integer> {

        public ManaBotainaCapabilityProxy(TileEntity tileEntity) {
            super(ManaBotainaCapability.CAP, tileEntity);
        }

        public IManaReceiver getCapability() {
            return (IManaReceiver)getTileEntity();
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IManaReceiver capability = getCapability();
            if (capability == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                int stored = capability.getCurrentMana();
                if (!simulate) {
                    capability.recieveMana(-stored);
                }
                sum = sum - stored;
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

    }
}
