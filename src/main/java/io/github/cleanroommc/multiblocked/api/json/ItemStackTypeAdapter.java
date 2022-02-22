package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.*;
import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;

public class ItemStackTypeAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    public static final JsonDeserializer<ItemStack> INSTANCE = new ItemStackTypeAdapter();

    private ItemStackTypeAdapter() { }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        JsonObject jsonObj = json.getAsJsonObject();
        String itemName = JsonUtils.getString(jsonObj, "id");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            Multiblocked.LOGGER.error("Item " + itemName + " does not exist!");
            return null;
        }
        int meta = JsonUtils.getInt(jsonObj, "damage", JsonUtils.getInt(jsonObj, "meta", 0));
        ItemStack itemStack = new ItemStack(item, JsonUtils.getInt(jsonObj, "count", 1), meta);
        if (JsonUtils.hasField(jsonObj, "nbt")) {
            try {
                itemStack.setTagCompound(JsonToNBT.getTagFromJson(Multiblocked.GSON.toJson(jsonObj.get("nbt"))));
            } catch (NBTException nbtexception) {
                Multiblocked.LOGGER.error("Invalid nbt tag: " + nbtexception.getMessage());
            }
        }
        return itemStack;
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src.getItem().getRegistryName() == null) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("id", src.getItem().getRegistryName().toString());
        jsonObj.addProperty("damage", src.getItemDamage());
        jsonObj.addProperty("count", src.getCount());
        if (src.hasTagCompound()) {
            jsonObj.add("nbt", Multiblocked.GSON.fromJson(src.getTagCompound().toString(), JsonElement.class));
        }
        return jsonObj;
    }
}
