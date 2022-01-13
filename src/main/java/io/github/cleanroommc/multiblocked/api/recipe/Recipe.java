package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;

import java.util.List;
import java.util.Map;

public class Recipe {
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Object>> inputs;
    public final ImmutableMap<MultiblockCapability<?>, ImmutableList<Object>> outputs;

    public Recipe(ImmutableMap<MultiblockCapability<?>, ImmutableList<Object>> inputs, ImmutableMap<MultiblockCapability<?>, ImmutableList<Object>> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Does the recipe match the owned proxy.
     *
     * @param capabilityProxies proxies
     * @return result
     */
    public boolean match(Table<IO, MultiblockCapability<?>, List<CapabilityProxy<?>>> capabilityProxies) {
        if (!match(IO.IN, capabilityProxies)) return false;
        return match(IO.OUT, capabilityProxies);
    }

    private boolean match(IO io, Table<IO, MultiblockCapability<?>, List<CapabilityProxy<?>>> capabilityProxies) {
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Object>> entry : io == IO.IN ? inputs.entrySet() : outputs.entrySet()) {
            for (Object content : entry.getValue()) {
                content = entry.getKey().copyContent(content);
                for (CapabilityProxy<?> proxy : capabilityProxies.get(io, entry.getKey())) { // search same io type
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
                if (content == null) break;
                for (CapabilityProxy<?> proxy : capabilityProxies.get(IO.BOTH, entry.getKey())) { // search both type
                    content = proxy.searchingRecipe(io, this, content);
                    if (content == null) break;
                }
                if (content != null) return false;
            }
        }
        return true;
    }
}
