package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.Tuple;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ZenClass("mods.multiblocked.recipe.Recipe")
@ZenRegister
public class Recipe {
    @ZenProperty
    public final String uid;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputs;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputs;
    public final int duration;

    public Recipe(String uid,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputs,
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
    public boolean match(Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        if (!match(IO.IN, capabilityProxies)) return false;
        return match(IO.OUT, capabilityProxies);
    }

    private boolean match(IO io, Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : io == IO.IN ? inputs.entrySet() : outputs.entrySet()) {
            List<?> content = entry.getValue().stream().map(Tuple::getFirst).collect(Collectors.toList());
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

    @SuppressWarnings("unchecked")
    public void handleInput(Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : inputs.entrySet()) {
            List content = new ArrayList<>();
            for (Tuple<Object, Float> tuple : entry.getValue()) {
                if (tuple.getSecond() == 1 || Multiblocked.RNG.nextFloat() < tuple.getSecond()) { // chance input
                    content.add(tuple.getFirst());
                }
            }
            if (content.isEmpty()) continue;
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

    @SuppressWarnings("unchecked")
    public void handleOutput(Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : outputs.entrySet()) {
            List content = new ArrayList<>();
            for (Tuple<Object, Float> tuple : entry.getValue()) {
                if (tuple.getSecond() == 1 || Multiblocked.RNG.nextFloat() < tuple.getSecond()) { // chance output
                    content.add(tuple.getFirst());
                }
            }
            if (content.isEmpty()) continue;
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
