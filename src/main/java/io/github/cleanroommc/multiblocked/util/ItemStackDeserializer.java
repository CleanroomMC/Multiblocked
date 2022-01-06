package io.github.cleanroommc.multiblocked.util;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;

public class ItemStackDeserializer implements JsonDeserializer<ItemStack> {

    public static final JsonDeserializer<ItemStack> INSTANCE = new ItemStackDeserializer();

    private ItemStackDeserializer() { }

    // TODO: Tag support soon, trying to think of a human readable yet conventional way
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();
        String itemName = JsonUtils.getString(jsonObj, "id");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            throw new IllegalArgumentException("Item " + itemName + " does not exist!");
        }
        int meta = JsonUtils.getInt(jsonObj, "Damage", JsonUtils.getInt(jsonObj, "Meta", 0));
        return new ItemStack(item, JsonUtils.getInt(jsonObj, "Count", 1), meta);
    }

}
