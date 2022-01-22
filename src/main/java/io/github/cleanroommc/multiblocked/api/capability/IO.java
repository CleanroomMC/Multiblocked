package io.github.cleanroommc.multiblocked.api.capability;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

/**
 * The capability can be input or output or both
 */
@ZenClass("mods.multiblocked.capability.IO")
@ZenRegister
public enum IO {
    @ZenProperty IN,
    @ZenProperty OUT,
    @ZenProperty BOTH;

    public static IO[] VALUES = IO.values();
}
