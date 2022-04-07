package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTController;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTPart;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;

@ZenRegister
@ZenExpansion("mods.multiblocked.tile.Component")
public class ExpandComponent {

    @ZenCaster
    public static ICTController asController(ICTComponent component) {
        return component instanceof ICTController ? (ICTController) component : null;
    }

    @ZenCaster
    public static ICTPart asPart(ICTComponent component) {
        return component instanceof ICTPart ? (ICTPart) component : null;
    }
}
