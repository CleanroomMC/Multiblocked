package io.github.cleanroommc.multiblocked.api.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

class FluidStackTypeAdapterTest {

    public static void main(String[] args) {
        deserialize();
    }

    static void deserialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("s","213");
        tag.setInteger("sdf",1213);
        tag.setTag("dd", new NBTTagCompound());
        Gson gson = new GsonBuilder().create();
        gson.fromJson(tag.toString(), JsonObject.class);
        System.out.println();
    }

    static void serialize() {
    }
}