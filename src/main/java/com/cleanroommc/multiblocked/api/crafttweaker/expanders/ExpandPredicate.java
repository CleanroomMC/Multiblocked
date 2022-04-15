package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.api.pattern.Predicates;
import com.cleanroommc.multiblocked.api.pattern.TraceabilityPredicate;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidDefinition;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.util.BlockInfo;
import net.minecraft.block.Block;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethodStatic;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

@ZenExpansion("mods.gregtech.multiblock.CTPredicate")
@ZenRegister
public class ExpandPredicate {
    /**
     * Can be any block.
     *
     * @return An {@link TraceabilityPredicate} that returns true for any of the given ANY predicate.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate getAny() {
        return Predicates.any();
    }

    /**
     * Only the air block.
     *
     * @return An {@link TraceabilityPredicate} that returns true for any of the given AIR predicate.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate getAir() {
        return Predicates.air();
    }


    private static Supplier<BlockInfo[]> getCandidates(Set<net.minecraft.block.state.IBlockState> allowedStates){
        return ()-> allowedStates.stream().map(state-> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }

    /**
     * Match any of the given {@link IBlockState}s.
     * <p>
     * When called with a single parameter, it is equivalent to {@code IBlockState as IBlockMatcher}.
     *
     * @param allowedStates The list of {@link IBlockState}s to match.
     * @return An {@link TraceabilityPredicate} that returns true for any of the given blockstates.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        return Predicates.states(Arrays.stream(allowedStates).map(CraftTweakerMC::getBlockState).toArray(net.minecraft.block.state.IBlockState[]::new));
    }

    /**
     * Match any blockstate with one of the given {@link IBlock}s.
     * <p>
     * When called with a single parameter, it is equivalent to {@code IBlock as IBlockMatcher}`
     *
     * @param blocks The list of {@link IBlock}s to match.
     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate blocks(IBlock... blocks) {
        return Predicates.blocks(Arrays.stream(blocks).map(CraftTweakerMC::getBlock).toArray(Block[]::new));
    }

    /**
     * Match any blockstate with one of the given {@link IItemStack}s.
     * <p>
     * When called with a single parameter, it is equivalent to {@code IItemStack as IBlock as IBlockMatcher}`
     *
     * @param itemStacks The list of {@link IItemStack}s to match.
     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate items(IItemStack... itemStacks) {
        return blocks(Arrays.stream(itemStacks).map(IItemStack::asBlock).toArray(IBlock[]::new));
    }

    /**
     * Match any blockstate with one of the given {@link ILiquidStack}s.
     * <p>
     * When called with a single parameter, it is equivalent to {@code ILiquidStack as IBlock as IBlockMatcher}`
     *
     * @param liquidStacks The list of {@link IItemStack}s to match.
     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
     */
    @ZenMethodStatic
    public static TraceabilityPredicate liquids(ILiquidStack... liquidStacks) {
        return blocks(Arrays.stream(liquidStacks).map(ILiquidStack::getDefinition).map(ILiquidDefinition::getBlock).toArray(IBlock[]::new));
    }

}
