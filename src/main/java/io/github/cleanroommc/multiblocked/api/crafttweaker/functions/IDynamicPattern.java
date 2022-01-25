package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.pattern.BlockPattern;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IDynamicPattern")
@ZenRegister
public interface IDynamicPattern {
    /**
     * even set dynamic pattern, you still have to set the baseppattern {@link io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition#basePattern}
     * @return null - to use the basePattern of the definition
     */
    BlockPattern apply(ControllerTileEntity controllerTileEntity);
}
