package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;

public class FluidStackTypeAdapter implements JsonDeserializer<FluidStack>, JsonSerializer<FluidStack> {

    public static final FluidStackTypeAdapter INSTANCE = new FluidStackTypeAdapter();

    private FluidStackTypeAdapter() { }

    @Override
    public FluidStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return FluidStack.loadFluidStackFromNBT(JsonToNBT.getTagFromJson(Multiblocked.GSON.toJson(json)));
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(FluidStack src, Type typeOfSrc, JsonSerializationContext context) {
        return Multiblocked.GSON.fromJson(src.writeToNBT(new NBTTagCompound()).toString(), JsonElement.class);
    }
}
