package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IDynamicRenderer")
@ZenRegister
public interface IDynamicRenderer {
    IRenderer apply(ComponentTileEntity<?> controllerTileEntity);
}
