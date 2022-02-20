package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.pattern.BlockPattern;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IDynamicPattern")
@ZenRegister
public interface IDynamicPattern {
    BlockPattern apply(ControllerTileEntity controllerTileEntity);
}
