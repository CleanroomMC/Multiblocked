package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IDynamicRenderer")
@ZenRegister
public interface IDynamicRenderer {
    IRenderer apply(ComponentTileEntity<?> controllerTileEntity);
}
