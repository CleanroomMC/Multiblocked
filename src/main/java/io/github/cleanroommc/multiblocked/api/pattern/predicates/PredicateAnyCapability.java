package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.pattern.error.PatternStringError;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PredicateAnyCapability extends SimplePredicate {
    public IO io;
    public String capability;

    public PredicateAnyCapability() { }
    
    public PredicateAnyCapability(IO io, MultiblockCapability<?> capability) {
        super(state -> state.getBlockState().getBlock() == capability.getAnyBlock(io) || checkCapability(io, capability, state),
                ()-> new BlockInfo[]{new BlockInfo(capability.getAnyBlock(io))});
        this.io = io;
        this.capability = capability.name;
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        MultiblockCapability<?> capability = MultiblockCapabilities.get(this.capability);
        predicate = state -> state.getBlockState().getBlock() == capability.getAnyBlock(io) || checkCapability(io, capability, state);
        candidates = () -> new BlockInfo[]{new BlockInfo(capability.getAnyBlock(io))};
        return this;
    }

    private static boolean checkCapability(IO io, MultiblockCapability<?> capability, MultiblockState state) {
        TileEntity tileEntity = state.getTileEntity();
        if (tileEntity != null && capability.isBlockHasCapability(io, tileEntity)) {
            Map<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> capabilities = state.getMatchContext().getOrCreate("capabilities", Long2ObjectOpenHashMap::new);
            capabilities.computeIfAbsent(state.getPos().toLong(), l-> new EnumMap<>(IO.class))
                    .computeIfAbsent(io, x->new HashSet<>())
                    .add(capability);
            return true;
        }
        if (Multiblocked.isClient()) {
            state.setError(new PatternStringError(
                    I18n.format("multiblocked.pattern.error.capability", I18n.format(capability.getUnlocalizedName()), io.name())));
        } else {
            state.setError(new PatternStringError("find no io_capability: " + io.name() + "_" + capability.name));
        }
        return false;
    }
}
