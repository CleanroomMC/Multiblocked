package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import net.minecraft.block.Block;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class PredicateBlocks extends SimplePredicate {
    public Block[] blocks;
    
    public PredicateBlocks() {}
    
    public PredicateBlocks(Block... blocks) {
        super(state -> ArrayUtils.contains(blocks, state.getBlockState().getBlock()),
                () -> Arrays.stream(blocks).map(block -> new BlockInfo(block.getDefaultState(), null)).toArray(BlockInfo[]::new));
        this.blocks = blocks;
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        predicate = state -> ArrayUtils.contains(blocks, state.getBlockState().getBlock());
        candidates = () -> Arrays.stream(blocks).map(block -> new BlockInfo(block.getDefaultState(), null)).toArray(BlockInfo[]::new);
        return this;
    }
}
