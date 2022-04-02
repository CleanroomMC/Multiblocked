package com.cleanroommc.multiblocked.api.capability;

import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass("mods.multiblocked.capability.CapabilityHolder")
@ZenRegister
public interface ICapabilityProxyHolder {

    @ZenGetter
    boolean hasProxies();
    Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> getCapabilities();
}
