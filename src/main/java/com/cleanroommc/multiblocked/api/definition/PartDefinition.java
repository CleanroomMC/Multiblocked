package com.cleanroommc.multiblocked.api.definition;

import com.cleanroommc.multiblocked.api.crafttweaker.functions.IPartAddedToMulti;
import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.api.crafttweaker.functions.IPartRemovedFromMulti;
import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

/**
 * Definition of a part.
 */
@ZenClass("mods.multiblocked.definition.PartDefinition")
@ZenRegister
public class PartDefinition extends ComponentDefinition {
    
    @ZenProperty
    public boolean canShared = true;
    @ZenProperty
    public transient IPartAddedToMulti partAddedToMulti;
    @ZenProperty
    public transient IPartRemovedFromMulti partRemovedFromMulti;

    // used for Gson
    public PartDefinition() {
        super(null, PartTileEntity.PartSimpleTileEntity.class);
    }

    public PartDefinition(ResourceLocation location, Class<? extends PartTileEntity<?>> clazz) {
        super(location, clazz);
    }

    public PartDefinition(ResourceLocation location) {
        super(location, PartTileEntity.PartSimpleTileEntity.class);
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        canShared = JsonUtils.getBoolean(json, "canShared", canShared);
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.addProperty("canShared", canShared);
        return json;
    }
}
