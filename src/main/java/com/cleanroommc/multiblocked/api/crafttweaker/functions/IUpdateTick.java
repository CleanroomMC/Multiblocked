package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IUpdateTick")
@ZenRegister
public interface IUpdateTick {
    void apply(ComponentTileEntity<?> controllerTileEntity);
}
