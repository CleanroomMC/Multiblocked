package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IPartAddedToMulti")
@ZenRegister
public interface IPartAddedToMulti {
    void apply(PartTileEntity<?> partTileEntity, ControllerTileEntity controllerTileEntity);
}
