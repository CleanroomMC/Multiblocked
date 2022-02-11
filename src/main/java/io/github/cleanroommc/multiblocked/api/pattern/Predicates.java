package io.github.cleanroommc.multiblocked.api.pattern;

import com.google.common.collect.Sets;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Predicates {
    private static Supplier<BlockInfo[]> getCandidates(Set<IBlockState> allowedStates){
        return ()-> allowedStates.stream().map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }

    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        return new TraceabilityPredicate(state -> ArrayUtils.contains(allowedStates, state.getBlockState()),
                getCandidates(Sets.newHashSet(allowedStates)));
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        return new TraceabilityPredicate(state -> ArrayUtils.contains(blocks, state.getBlockState().getBlock()),
                getCandidates(Arrays.stream(blocks).map(Block::getDefaultState).collect(
                Collectors.toSet())));
    }

    /**
     * Use it when you require that a position must have a specific capability.
     */
    public static TraceabilityPredicate anyCapability(IO io, MultiblockCapability<?> capability) {
        Block block = MultiblockComponents.getOrRegisterAnyCapabilityBlock(io, capability);
        return new TraceabilityPredicate(state -> {
            if (state.getBlockState().getBlock() == block)  return true;
            return checkCapability(io, capability, state);
        }, ()-> new BlockInfo[]{new BlockInfo(block)});
    }

    /**
     * Use it when you specify that certain blocks provide only certain capabilities.
     * <br>
     * Otherwise all capacities of it which meet the recipeMap needs will be captured.
     */
    public static TraceabilityPredicate blocksWithCapability(IO io, MultiblockCapability<?> capability, Block... blocks) {
        if (blocks.length == 0) return anyCapability(io, capability);
        return new TraceabilityPredicate(state -> {
            if (!ArrayUtils.contains(blocks, state.getBlockState().getBlock())) return false;
            return checkCapability(io, capability, state);
        }, getCandidates(Arrays.stream(blocks).map(Block::getDefaultState).collect(Collectors.toSet())));
    }

    /**
     * Use it when you specify that certain blocks provide only certain capabilities.
     * <br>
     * Otherwise all capacities of it which meet the recipeMap needs will be captured.
     */
    public static TraceabilityPredicate statesWithCapability(IO io, MultiblockCapability<?> capability, IBlockState... allowedStates) {
        if (allowedStates.length == 0) return anyCapability(io, capability);
        return new TraceabilityPredicate(state -> {
            if (!ArrayUtils.contains(allowedStates, state.getBlockState())) return false;
            return checkCapability(io, capability, state);
        }, getCandidates(Sets.newHashSet(allowedStates)));
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
            state.setError(new PatternStringError(I18n.format("multiblocked.pattern.error.capability", I18n.format(capability.getUnlocalizedName()), io.name())));
        } else {
            state.setError(new PatternStringError("find no io_capability: " + io.name() + "_" + capability.name));
        }
        return false;
    }

    public static TraceabilityPredicate any() {
        return TraceabilityPredicate.ANY;
    }

    public static TraceabilityPredicate air() {
        return TraceabilityPredicate.AIR;
    }

}
