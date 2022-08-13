package com.cleanroommc.multiblocked.api.capability;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.*;

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

    @ZenOperator(OperatorType.EQUALS)
    public boolean equals(IO io) {
        return this == io;
    }
}
