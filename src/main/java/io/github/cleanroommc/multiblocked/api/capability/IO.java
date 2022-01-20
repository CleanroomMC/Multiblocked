package io.github.cleanroommc.multiblocked.api.capability;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

/**
 * The capability can be input or output or both
 */
@ZenClass("mods.multiblocked.capability.IO")
@ZenRegister
public enum IO {
    IN,
    OUT,
    BOTH;

    public static IO[] VALUES = IO.values();
}
