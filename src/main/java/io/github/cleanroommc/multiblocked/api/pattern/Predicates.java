package io.github.cleanroommc.multiblocked.api.pattern;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Predicates {
    private static Supplier<BlockInfo[]> getCandidates(
            Set<IBlockState> allowedStates){
        return ()-> allowedStates.stream().map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }

    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        Set<IBlockState> states = new ObjectOpenHashSet<>(Arrays.asList(allowedStates));
        return new TraceabilityPredicate(blockWorldState -> states.contains(blockWorldState.getBlockState()), getCandidates(states));
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        Set<Block> bloxx = new ObjectOpenHashSet<>(Arrays.asList(blocks));
        return new TraceabilityPredicate(blockWorldState -> bloxx.contains(blockWorldState.getBlockState().getBlock()), getCandidates(bloxx.stream().map(Block::getDefaultState).collect(
                Collectors.toSet())));
    }

}
