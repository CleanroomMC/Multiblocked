package io.github.cleanroommc.multiblocked.api.definition;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IPartAddedToMulti;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IPartRemovedFromMulti;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
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

}
