package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.ICapabilityProxyHolder;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ZenClass("mods.multiblocked.recipe.Recipe")
@ZenRegister
public class Recipe {
    public static final ImmutableMap<String, Object> EMPTY = ImmutableMap.of();
    @ZenProperty
    public final String uid;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputs;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputs;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickInputs;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickOutputs;
    public final ImmutableMap<String, Object> data;
    @ZenProperty
    public final int duration;
    public final ITextComponent text;

    public Recipe(String uid,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickInputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickOutputs,
                  int duration) {
        this(uid, inputs, outputs, tickInputs, tickOutputs, EMPTY, null, duration);
    }

    public Recipe(String uid,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickInputs,
                  ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickOutputs,
                  ImmutableMap<String, Object> data,
                  ITextComponent text,
                  int duration) {
        this.uid = uid;
        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;
        this.duration = duration;
        this.data = data;
        this.text = text;
    }

    public <T> T getData(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        return null;
    }

    @ZenMethod
    public List<Content> getInputContents(MultiblockCapability<?> capability) {
        if (inputs.containsKey(capability)) {
            return inputs.get(capability).stream().map(Content::new).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @ZenMethod
    public List<Content> getOutputContents(MultiblockCapability<?> capability) {
        if (outputs.containsKey(capability)) {
            return outputs.get(capability).stream().map(Content::new).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Does the recipe match the owned proxy.
     *
     * @param holder proxies
     * @return result
     */
    @ZenMethod
    public boolean matchRecipe(ICapabilityProxyHolder holder) {
        if (!holder.hasProxies()) return false;
        if (!matchRecipe(IO.IN, holder.getCapabilities(), inputs)) return false;
        if (!matchRecipe(IO.OUT, holder.getCapabilities(), outputs)) return false;
        return true;
    }

    @ZenMethod
    public boolean matchTickRecipe(ICapabilityProxyHolder holder) {
        if (hasTick()) {
            if (!holder.hasProxies()) return false;
            if (!matchRecipe(IO.IN, holder.getCapabilities(), tickInputs)) return false;
            if (!matchRecipe(IO.OUT, holder.getCapabilities(), tickOutputs)) return false;
        }
        return true;
    }

    private boolean matchRecipe(IO io, Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies, ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> contents) {
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : contents.entrySet()) {
            Set<CapabilityProxy<?>> used = new HashSet<>();
            List<?> content = entry.getValue().stream().map(Tuple::getFirst).collect(Collectors.toList());
            if (capabilityProxies.contains(io, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(io, entry.getKey()).values()) { // search same io type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
            }
            if (content == null) continue;
            if (capabilityProxies.contains(IO.BOTH, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey()).values()) { // search both type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
            }
            if (content != null) return false;
        }
        return true;
    }

    public boolean handleTickRecipeIO(IO io, ICapabilityProxyHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? tickInputs : tickOutputs);
    }

    public boolean handleRecipeIO (IO io, ICapabilityProxyHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? inputs : outputs);
    }

    @SuppressWarnings("ALL")
    @ZenMethod
    public boolean handleRecipe(IO io, ICapabilityProxyHolder holder, ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> contents) {
        Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies = holder.getCapabilities();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : contents.entrySet()) {
            Set<CapabilityProxy<?>> used = new HashSet<>();
            List content = new ArrayList<>();
            for (Tuple<Object, Float> tuple : entry.getValue()) {
                if (tuple.getSecond() == 1 || Multiblocked.RNG.nextFloat() < tuple.getSecond()) { // chance input
                    content.add(tuple.getFirst());
                }
            }
            if (content.isEmpty()) continue;
            if (capabilityProxies.contains(io, entry.getKey())) {
                for (CapabilityProxy<?> proxy : capabilityProxies.get(io, entry.getKey()).values()) { // search same io type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    content = proxy.handleRecipe(io, this, content);
                    if (content == null) break;
                }
            }
            if (content == null) continue;
            if (capabilityProxies.contains(IO.BOTH, entry.getKey())){
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey()).values()) { // search both type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    content = proxy.handleRecipe(io,this, content);
                    if (content == null) break;
                }
            }
            if (content != null) {
                Multiblocked.LOGGER.warn("io error while handling a recipe {} outputs. holder: {}", uid, holder);
                return false;
            }
        }
        return true;
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }
}
