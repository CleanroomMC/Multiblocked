package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.common.capability.widget.ItemsContentWidget;
import com.google.gson.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class ItemMultiblockCapability extends MultiblockCapability<ItemsIngredient> {

    public ItemMultiblockCapability() {
        super("item", new Color(0xD96106).getRGB());
    }

    @Override
    public ItemsIngredient defaultContent() {
        return new ItemsIngredient(1, Items.IRON_INGOT.getDefaultInstance());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, tileEntity).isEmpty();
    }

    @Override
    public ItemsIngredient copyInner(ItemsIngredient content) {
        return content.copy();
    }

    @Override
    public ItemCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ItemCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super ItemsIngredient> createContentWidget() {
        return new ItemsContentWidget();
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {
                BlockInfo.fromBlockState(Blocks.CHEST.getDefaultState()),
                new BlockInfo(Blocks.WHITE_SHULKER_BOX.getDefaultState(), Blocks.WHITE_SHULKER_BOX.createTileEntity(null, Blocks.WHITE_SHULKER_BOX.getDefaultState()), new ItemStack(Blocks.WHITE_SHULKER_BOX))
        };
    }

    @Override
    public ItemsIngredient deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has("ore")) {
            return new ItemsIngredient(jsonObject.get("amount").getAsInt(), jsonObject.get("ore").getAsString());
        } else {
            return new ItemsIngredient(jsonObject.get("amount").getAsInt(), Multiblocked.GSON.fromJson(jsonObject.get("matches"), ItemStack[].class));
        }
    }

    @Override
    public JsonElement serialize(ItemsIngredient itemsIngredient, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", itemsIngredient.getAmount());
        if (itemsIngredient.isOre()) {
            jsonObject.addProperty("ore", itemsIngredient.getOreDict());
        } else {
            jsonObject.add("matches", Multiblocked.GSON.toJsonTree(itemsIngredient.matchingStacks));
        }
        return jsonObject;
    }

    public static class ItemCapabilityProxy extends CapCapabilityProxy<IItemHandler, ItemsIngredient> {

        public ItemCapabilityProxy(TileEntity tileEntity) {
            super(MbdCapabilities.ITEM, tileEntity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        }

        @Override
        protected List<ItemsIngredient> handleRecipeInner(IO io, Recipe recipe, List<ItemsIngredient> left, boolean simulate) {
            IItemHandler capability = getCapability();
            if (capability == null) return left;
            Iterator<ItemsIngredient> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    ItemsIngredient ingredient = iterator.next();
                    for (int i = 0; i < capability.getSlots(); i++) {
                        ItemStack itemStack = capability.getStackInSlot(i);
                        if (ingredient.apply(itemStack)) {
                            ItemStack extracted = capability.extractItem(i, ingredient.getAmount(), simulate);
                            ingredient.setAmount(ingredient.getAmount() - extracted.getCount());
                            if (ingredient.getAmount() <= 0) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
            } else if (io == IO.OUT){
                while (iterator.hasNext()) {
                    ItemsIngredient ingredient = iterator.next();
                    ItemStack output = ingredient.getOutputStack();
                    for (int i = 0; i < capability.getSlots(); i++) {
                        output = capability.insertItem(i, output, simulate);
                        if (output.isEmpty()) break;
                    }
                    if (output.isEmpty()) iterator.remove();
                    else ingredient.setAmount(output.getCount());
                }
            }
            return left.isEmpty() ? null : left;
        }

    }
}
