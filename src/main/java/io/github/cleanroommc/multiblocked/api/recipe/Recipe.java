package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.List;
import java.util.Map;

@ZenClass("mods.multiblocked.recipe.Recipe")
@ZenRegister
public class Recipe {
    @ZenProperty
    public final String uid;
    public final ImmutableMap<MultiblockCapability, ImmutableList<Object>> inputs;
    public final ImmutableMap<MultiblockCapability, ImmutableList<Object>> outputs;
    public final int duration;

    public Recipe(String uid,
                  ImmutableMap<MultiblockCapability, ImmutableList<Object>> inputs,
                  ImmutableMap<MultiblockCapability, ImmutableList<Object>> outputs,
                  int duration) {
        this.uid = uid;
        this.inputs = inputs;
        this.outputs = outputs;
        this.duration = duration;
    }

    /**
     * Does the recipe match the owned proxy.
     *
     * @param capabilityProxies proxies
     * @return result
     */
    public boolean match(Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        if (!match(IO.IN, capabilityProxies)) return false;
        return match(IO.OUT, capabilityProxies);
    }

    private boolean match(IO io, Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability, ImmutableList<Object>> entry : io == IO.IN ? inputs.entrySet() : outputs.entrySet()) {
            List<?> content = entry.getValue();
            if (capabilityProxies.contains(io, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(io, entry.getKey()).values()) { // search same io type
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
            }
            if (content == null) continue;
            if (capabilityProxies.contains(IO.BOTH, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey()).values()) { // search both type
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
            }
            if (content != null) return false;
        }
        return true;
    }

    public void handleInput(Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability, ImmutableList<Object>> entry : inputs.entrySet()) {
            List<?> content = entry.getValue();
            if (capabilityProxies.contains(IO.IN, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.IN, entry.getKey()).values()) { // search same io type
                    content = proxy.handleRecipeInput(this, content);
                    if (content == null) break;
                }
            }
            if (content == null) continue;
            if (capabilityProxies.contains(IO.BOTH, entry.getKey())){
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey()).values()) { // search both type
                    content = proxy.handleRecipeInput(this, content);
                    if (content == null) break;
                }
            }
        }
    }

    public void handleOutput(Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability, ImmutableList<Object>> entry : outputs.entrySet()) {
            List<?> content = entry.getValue();
            if (capabilityProxies.contains(IO.OUT, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.OUT, entry.getKey()).values()) { // search same io type
                    content = proxy.handleRecipeOutput(this, content);
                    if (content == null) break;
                }
            }
            if (content == null) continue;
            if (capabilityProxies.contains(IO.BOTH, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey()).values()) { // search both type
                    content = proxy.handleRecipeOutput(this, content);
                    if (content == null) break;
                }
            }
        }
    }
}
