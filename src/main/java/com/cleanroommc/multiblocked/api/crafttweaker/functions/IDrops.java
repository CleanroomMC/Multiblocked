package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.player.IPlayer;
import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IDrops")
@ZenRegister
public interface IDrops {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    IItemStack[] apply(ICTComponent componentTileEntity, IPlayer player);
}
