package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.multiblocked.tile.Part")
@ZenRegister
public interface ICTPart extends ICTComponent {
    
    PartTileEntity<?> getInner();
    
    @ZenGetter
    default List<ICTController> controllers() {
        return new ArrayList<>(getInner().getControllers());
    }
}
