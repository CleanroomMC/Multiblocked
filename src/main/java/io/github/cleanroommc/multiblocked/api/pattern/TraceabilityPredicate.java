package io.github.cleanroommc.multiblocked.api.pattern;

import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TraceabilityPredicate {

    public List<SimplePredicate> common = new ArrayList<>();
    public List<SimplePredicate> limited = new ArrayList<>();
    public boolean isCenter;

    public TraceabilityPredicate() {}

    public TraceabilityPredicate(TraceabilityPredicate predicate) {
        common.addAll(predicate.common);
        limited.addAll(predicate.limited);
        isCenter = predicate.isCenter;
    }

    public TraceabilityPredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        common.add(new SimplePredicate(predicate, candidates));
    }

    public TraceabilityPredicate(SimplePredicate simplePredicate) {
        if (simplePredicate.minGlobalCount != -1 || simplePredicate.maxGlobalCount != -1|| simplePredicate.minLayerCount != -1|| simplePredicate.maxLayerCount != -1) {
            limited.add(simplePredicate);
        } else {
            common.add(simplePredicate);
        }
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
            newPredicate.common.addAll(other.common);
            newPredicate.limited.addAll(other.limited);
            return newPredicate;
        }
        return this;
    }

    public boolean isAny() {
        return this.common.size() == 1 && this.limited.isEmpty() && this.common.get(0) == SimplePredicate.ANY;
    }

    public boolean isAir() {
        return this.common.size() == 1 && this.limited.isEmpty() && this.common.get(0) == SimplePredicate.AIR;
    }

    public boolean isSingle() {
        return !isAny() && !isAir() && this.common.size() + this.limited.size() == 1;
    }

    public boolean hasAir() {
        return this.common.contains(SimplePredicate.AIR);
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
            return I18n.format("multiblocked.pattern.error.limited." + type, number);
        }
    }
}
