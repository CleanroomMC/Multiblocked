package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

import java.lang.reflect.Type;

public class ItemStackTypeAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    public static final ItemStackTypeAdapter INSTANCE = new ItemStackTypeAdapter();

    private ItemStackTypeAdapter() { }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return new ItemStack(JsonToNBT.getTagFromJson(Multiblocked.GSON.toJson(json)));
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        return Multiblocked.GSON.fromJson(src.serializeNBT().toString(), JsonElement.class);
    }
}
