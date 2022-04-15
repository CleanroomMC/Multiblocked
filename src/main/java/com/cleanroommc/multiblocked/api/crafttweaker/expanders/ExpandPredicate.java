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
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.VariantActiveBlock;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenOperator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ZenExpansion("mods.gregtech.multiblock.CTPredicate")
@ZenRegister
public class ExpandPredicate {
    /**
     * Can be any block.
     *
     * @return An {@link TraceabilityPredicate} that returns true for any of the given ANY predicate.
     */
    @ZenMethod
    public static TraceabilityPredicate getAny() {
        return Predicates.any();
    }

    /**
     * Only the air block.
     *
     * @return An {@link TraceabilityPredicate} that returns true for any of the given AIR predicate.
     */
    @ZenMethod
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
    @ZenMethod
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
    @ZenMethod
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
    @ZenMethod
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
    @ZenMethod
    public static TraceabilityPredicate liquids(ILiquidStack... liquidStacks) {
        return blocks(Arrays.stream(liquidStacks).map(ILiquidStack::getDefinition).map(ILiquidDefinition::getBlock).toArray(IBlock[]::new));
    }

}
