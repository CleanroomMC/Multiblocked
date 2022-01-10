package io.github.cleanroommc.multiblocked.api.pattern;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TraceabilityPredicate {

    // Allow any block.
    public static TraceabilityPredicate ANY =new TraceabilityPredicate((state)->true);
    // Allow the air block.
    public static TraceabilityPredicate AIR = new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock().isAir(blockWorldState.getBlockState(), blockWorldState.getWorld(), blockWorldState.getPos()));

    public final List<SimplePredicate> common = new ArrayList<>();
    public final List<SimplePredicate> limited = new ArrayList<>();
    protected boolean isCenter;
    protected boolean hasAir = false;
    protected boolean isSingle = true;

    public TraceabilityPredicate() {}

    public TraceabilityPredicate(TraceabilityPredicate predicate) {
        common.addAll(predicate.common);
        limited.addAll(predicate.limited);
        isCenter = predicate.isCenter;
        hasAir = predicate.hasAir;
        isSingle = predicate.isSingle;
    }

    public TraceabilityPredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        common.add(new SimplePredicate(predicate, candidates));
    }

    public TraceabilityPredicate(Predicate<MultiblockState> predicate) {
        this(predicate, null);
    }

    public boolean isHasAir() {
        return hasAir;
    }

    public boolean isSingle() {
        return isSingle;
    }

    /**
     * Mark it as the controller of this multi. Normally you won't call it yourself. Use plz.
     */
    public TraceabilityPredicate setCenter() {
        isCenter = true;
        return this;
    }

    public TraceabilityPredicate sort() {
        limited.sort(Comparator.comparingInt(a -> ((a.minLayerCount + 1) * 100 + a.minGlobalCount)));
        return this;
    }

    /**
     * Add tooltips for candidates. They are shown in JEI Pages.
     */
    public TraceabilityPredicate addTooltips(String... tips) {
        if (tips.length > 0) {
            List<String> tooltips = Arrays.stream(tips).collect(Collectors.toList());
            common.forEach(predicate -> {
                if (predicate.candidates == null) return;
                if (predicate.toolTips == null) {
                    predicate.toolTips = new ArrayList<>();
                }
                predicate.toolTips.addAll(tooltips);
            });
            limited.forEach(predicate -> {
                if (predicate.candidates == null) return;
                if (predicate.toolTips == null) {
                    predicate.toolTips = new ArrayList<>();
                }
                predicate.toolTips.addAll(tooltips);
            });
        }
        return this;
    }

    /**
     * Set the minimum number of candidate blocks.
     */
    public TraceabilityPredicate setMinGlobalLimited(int min) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minGlobalCount = min;
        }
        return this;
    }

    public TraceabilityPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinGlobalLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks.
     */
    public TraceabilityPredicate setMaxGlobalLimited(int max) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxGlobalCount = max;
        }
        return this;
    }

    public TraceabilityPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxGlobalLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Set the minimum number of candidate blocks for each aisle layer.
     */
    public TraceabilityPredicate setMinLayerLimited(int min) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minLayerCount = min;
        }
        return this;
    }

    public TraceabilityPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinLayerLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks for each aisle layer.
     */
    public TraceabilityPredicate setMaxLayerLimited(int max) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxLayerCount = max;
        }
        return this;
    }

    public TraceabilityPredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxLayerLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     * @param limit The Maximum and Minimum limit
     */
    public TraceabilityPredicate setExactLimit(int limit) {
        return this.setMinGlobalLimited(limit).setMaxGlobalLimited(limit);
    }

    /**
     * Set the number of it appears in JEI pages. It only affects JEI preview. (The specific number)
     */
    public TraceabilityPredicate setPreviewCount(int count) {
        common.forEach(predicate -> predicate.previewCount = count);
        limited.forEach(predicate -> predicate.previewCount = count);
        return this;
    }

    public boolean test(MultiblockState blockWorldState) {
        boolean flag = false;
        for (SimplePredicate predicate : limited) {
            if (predicate.testLimited(blockWorldState)) {
                flag = true;
            }
        }
        return flag || common.stream().anyMatch(predicate->predicate.test(blockWorldState));
    }

    public TraceabilityPredicate or(TraceabilityPredicate other) {
        if (other != null) {
            TraceabilityPredicate newPredicate = new TraceabilityPredicate(this);
            if (this != AIR && other != AIR) {
                newPredicate.isSingle = false;
            } else {
                newPredicate.isSingle = this.isSingle && other.isSingle;
            }
            newPredicate.hasAir = newPredicate.hasAir || this == AIR || other == AIR;
            newPredicate.common.addAll(other.common);
            newPredicate.limited.addAll(other.limited);
            return newPredicate;
        }
        return this;
    }

    public static class SimplePredicate{
        public final Supplier<BlockInfo[]> candidates;

        public final Predicate<MultiblockState> predicate;

        private List<String> toolTips;

        public int minGlobalCount = -1;
        public int maxGlobalCount = -1;
        public int minLayerCount = -1;
        public int maxLayerCount = -1;

        public int previewCount = -1;

        public SimplePredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
            this.predicate = predicate;
            this.candidates = candidates;
        }

        @SideOnly(Side.CLIENT)
        public List<String> getToolTips(TraceabilityPredicate predicates) {
            List<String> result = new ArrayList<>();
            if (toolTips != null) {
                toolTips.forEach(tip->result.add(I18n.format(tip)));
            }
            if (minGlobalCount == maxGlobalCount && maxGlobalCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited_exact", minGlobalCount));
            } else if (minGlobalCount != maxGlobalCount && minGlobalCount != -1 && maxGlobalCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited_within", minGlobalCount, maxGlobalCount));
            } else {
                if (minGlobalCount != -1) {
                    result.add(I18n.format("gregtech.multiblock.pattern.error.limited.1", minGlobalCount));
                }
                if (maxGlobalCount != -1) {
                    result.add(I18n.format("gregtech.multiblock.pattern.error.limited.0", maxGlobalCount));
                }
            }
            if (minLayerCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited.3", minLayerCount));
            }
            if (maxLayerCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited.2", maxLayerCount));
            }
            if (predicates == null) return result;
            if (predicates.isSingle) {
                result.add(I18n.format("gregtech.multiblock.pattern.single"));
            }
            if (predicates.hasAir) {
                result.add(I18n.format("gregtech.multiblock.pattern.replaceable_air"));
            }
            return result;
        }

        public boolean test(MultiblockState blockWorldState) {
            return predicate.test(blockWorldState);
        }

        public boolean testLimited(MultiblockState blockWorldState) {
            return testGlobal(blockWorldState) && testLayer(blockWorldState);
        }

        public boolean testGlobal(MultiblockState blockWorldState) {
            if (minGlobalCount == -1 && maxGlobalCount == -1) return true;
            Integer count = blockWorldState.globalCount.get(this);
            boolean base = predicate.test(blockWorldState);
            count = (count == null ? 0 : count) + (base ? 1 : 0);
            blockWorldState.globalCount.put(this, count);
            if (maxGlobalCount == -1 || count <= maxGlobalCount) return base;
            blockWorldState.setError(new SinglePredicateError(this, 0));
            return false;
        }

        public boolean testLayer(MultiblockState blockWorldState) {
            if (minLayerCount == -1 && maxLayerCount == -1) return true;
            Integer count = blockWorldState.layerCount.get(this);
            boolean base = predicate.test(blockWorldState);
            count = (count == null ? 0 : count) + (base ? 1 : 0);
            blockWorldState.layerCount.put(this, count);
            if (maxLayerCount == -1 || count <= maxLayerCount) return base;
            blockWorldState.setError(new SinglePredicateError(this, 2));
            return false;
        }

        public List<ItemStack> getCandidates() {
            return candidates == null ? Collections.emptyList() : Arrays.stream(this.candidates.get()).filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info->{
                IBlockState blockState = info.getBlockState();
                //TODO
//                MetaTileEntity metaTileEntity = info.getTileEntity() instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity() : null;
//                if (metaTileEntity != null) {
//                    return metaTileEntity.getStackForm();
//                } else {
                    return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
//                }
            }).collect(Collectors.toList());
        }
    }

    public static class SinglePredicateError extends PatternError {
        public final SimplePredicate predicate;
        public final int type;

        public SinglePredicateError(SimplePredicate predicate, int type) {
            this.predicate = predicate;
            this.type = type;
        }

        @Override
        public List<List<ItemStack>> getCandidates() {
            return Collections.singletonList(predicate.getCandidates());
        }

        @SideOnly(Side.CLIENT)
        @Override
        public String getErrorInfo() {
            int number = -1;
            if (type == 0) number = predicate.maxGlobalCount;
            if (type == 1) number = predicate.minGlobalCount;
            if (type == 2) number = predicate.maxLayerCount;
            if (type == 3) number = predicate.minLayerCount;
            return I18n.format("gregtech.multiblock.pattern.error.limited." + type, number);
        }
    }


    private static Supplier<BlockInfo[]> getCandidates(Set<IBlockState> allowedStates){
        return ()-> allowedStates.stream().map(state-> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }
//
//    /**
//     * Match any of the given {@link IBlockState}s.
//     * <p>
//     * When called with a single parameter, it is equivalent to {@code IBlockState as IBlockMatcher}.
//     *
//     * @param allowedStates The list of {@link IBlockState}s to match.
//     * @return An {@link TraceabilityPredicate} that returns true for any of the given blockstates.
//     */
//    @ZenMethod
    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        Set<IBlockState> states = new ObjectOpenHashSet<>(Arrays.asList(allowedStates));
        return new TraceabilityPredicate(blockWorldState -> states.contains(blockWorldState.getBlockState()), getCandidates(states));
    }
//
//    /**
//     * Match any blockstate with one of the given {@link IBlock}s.
//     * <p>
//     * When called with a single parameter, it is equivalent to {@code IBlock as IBlockMatcher}`
//     *
//     * @param blocks The list of {@link IBlock}s to match.
//     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
//     */
//    @ZenMethod
    public static TraceabilityPredicate blocks(Block... blocks) {
        Set<Block> bloxx = new ObjectOpenHashSet<>(Arrays.asList(blocks));
        return new TraceabilityPredicate(blockWorldState -> bloxx.contains(blockWorldState.getBlockState().getBlock()), getCandidates(bloxx.stream().map(Block::getDefaultState).collect(Collectors.toSet())));
    }
//
//    /**
//     * Match any blockstate with one of the given {@link IItemStack}s.
//     * <p>
//     * When called with a single parameter, it is equivalent to {@code IItemStack as IBlock as IBlockMatcher}`
//     *
//     * @param itemStacks The list of {@link IItemStack}s to match.
//     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
//     */
//    @ZenMethod
//    public static TraceabilityPredicate items(ItemStack... itemStacks) {
//        return blocks(Arrays.stream(itemStacks).map(ItemStack::asBlock).toArray(IBlock[]::new));
//    }
//
//    /**
//     * Match any blockstate with one of the given {@link ILiquidStack}s.
//     * <p>
//     * When called with a single parameter, it is equivalent to {@code ILiquidStack as IBlock as IBlockMatcher}`
//     *
//     * @param liquidStacks The list of {@link IItemStack}s to match.
//     * @return An {@link TraceabilityPredicate} that returns true for any of the given blocks.
//     */
//    @ZenMethod
//    public static TraceabilityPredicate liquids(ILiquidStack... liquidStacks) {
//        return blocks(Arrays.stream(liquidStacks).map(ILiquidStack::getDefinition).map(ILiquidDefinition::getBlock).toArray(IBlock[]::new));
//    }
}
