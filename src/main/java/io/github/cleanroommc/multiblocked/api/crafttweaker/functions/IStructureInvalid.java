package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IStructureInvalid")
@ZenRegister
public interface IStructureInvalid {
    void apply(ControllerTileEntity controllerTileEntity);
}
