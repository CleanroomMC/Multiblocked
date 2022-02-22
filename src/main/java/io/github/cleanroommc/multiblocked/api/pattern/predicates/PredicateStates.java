package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.pattern.BlockInfo;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class PredicateStates extends SimplePredicate {
    public IBlockState[] states;

    public PredicateStates() {}
    
    public PredicateStates(IBlockState... states) {
        super(state -> ArrayUtils.contains(states, state.getBlockState()),
                () -> Arrays.stream(states).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new));
        this.states = states;
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        predicate = state -> ArrayUtils.contains(states, state.getBlockState());
        candidates = () -> Arrays.stream(states).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
        return this;
    }
}
