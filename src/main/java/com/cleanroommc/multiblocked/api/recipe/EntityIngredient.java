package com.cleanroommc.multiblocked.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2022/8/22
 * @implNote EntityIngredient
 */
public class EntityIngredient {
    public static final EntityEntry ITEM = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("item"));
    public EntityEntry type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("pig"));
    public NBTTagCompound tag;

    public boolean match(Entity entity) {
        if (net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entity.getClass()) != type) return false;
        if (tag != null) {
            NBTTagCompound nbt = entity.serializeNBT();
            NBTTagCompound merged = nbt.copy();
            merged.merge(tag);
            return nbt.equals(merged);
        }
        return true;
    }

    public static EntityIngredient fromJson(JsonElement json) {
        EntityIngredient ingredient = new EntityIngredient();
        if (json.isJsonPrimitive()) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.getAsString()));
            ingredient.type = entry == null ? ingredient.type : entry;
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(JsonUtils.getString(object, "type")));
            ingredient.type = entry == null ? ingredient.type : entry;
            if (object.has("tag")) {
                try {
                    ingredient.tag = JsonToNBT.getTagFromJson(object.get("tag").getAsString());
                } catch (NBTException ignored) {
                }
            }
        }
        return ingredient;
    }

    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.getRegistryName().toString());
        if (tag != null) {
            object.addProperty("tag", tag.toString());
        }
        return object;
    }

    public static EntityIngredient of(Object o) {
        EntityIngredient ingredient = new EntityIngredient();
        if (o instanceof EntityIngredient) {
            ingredient = (EntityIngredient) o;
        } else if (o instanceof CharSequence) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(o.toString()));
            ingredient.type = entry == null ? ingredient.type : entry;
        } else if (o instanceof ResourceLocation) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(o.toString()));
            ingredient.type = entry == null ? ingredient.type : entry;
        }
        return ingredient;
    }

    public void spawn(World serverLevel, NBTTagCompound tag, BlockPos pos) {
        Entity entity = type.newInstance(serverLevel);
        entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (tag != null) {
            NBTTagCompound nbt = entity.serializeNBT();
            nbt.merge(tag);
            entity.deserializeNBT(nbt);
        }
        serverLevel.spawnEntity(entity);
    }

    public EntityIngredient copy() {
        EntityIngredient copy = new EntityIngredient();
        copy.type = type;
        if (tag != null) {
            copy.tag = tag.copy();
        }
        return copy;
    }

    public boolean isEntityItem() {
        return type == ITEM && tag != null  && tag.hasKey("Item");
    }

    public ItemStack getEntityItem() {
        return new ItemStack(tag.getCompoundTag("Item"));
    }
}
