package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.pattern.error.SinglePredicateError;
import io.github.cleanroommc.multiblocked.api.pattern.TraceabilityPredicate;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimplePredicate {
    public static SimplePredicate ANY = new SimplePredicate("any", x -> true, null);
    public static SimplePredicate AIR = new SimplePredicate("air", blockWorldState -> blockWorldState.getBlockState().getBlock().isAir(blockWorldState.getBlockState(), blockWorldState.getWorld(), blockWorldState.getPos()), null);
    public transient Supplier<BlockInfo[]> candidates;

    public transient Predicate<MultiblockState> predicate;

    public List<String> toolTips;

    public int minGlobalCount = -1;
    public int maxGlobalCount = -1;

    public int previewCount = -1;
    public final String type;

    public SimplePredicate() {
        this("unknown");
    }
    
    public SimplePredicate(String type) {
        this.type = type;
    }

    public SimplePredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        this();
        this.predicate = predicate;
        this.candidates = candidates;
    }

    public SimplePredicate(String type, Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        this(type);
        this.predicate = predicate;
        this.candidates = candidates;
    }

    public SimplePredicate buildObjectFromJson() {
        return this;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getToolTips(TraceabilityPredicate predicates) {
        List<String> result = new ArrayList<>();
        if (toolTips != null) {
            toolTips.forEach(tip->result.add(I18n.format(tip)));
        }
        if (minGlobalCount == maxGlobalCount && maxGlobalCount != -1) {
            result.add(I18n.format("multiblocked.pattern.limited_exact", minGlobalCount));
        } else if (minGlobalCount != maxGlobalCount && minGlobalCount != -1 && maxGlobalCount != -1) {
            result.add(I18n.format("multiblocked.pattern.limited_within", minGlobalCount, maxGlobalCount));
        } else {
            if (minGlobalCount != -1) {
                result.add(I18n.format("multiblocked.pattern.error.limited.1", minGlobalCount));
            }
            if (maxGlobalCount != -1) {
                result.add(I18n.format("multiblocked.pattern.error.limited.0", maxGlobalCount));
            }
        }
        if (predicates == null) return result;
        if (predicates.isSingle()) {
            result.add(I18n.format("multiblocked.pattern.single"));
        }
        if (predicates.hasAir()) {
            result.add(I18n.format("multiblocked.pattern.replaceable_air"));
        }
        return result;
    }

    public boolean test(MultiblockState blockWorldState) {
        return predicate.test(blockWorldState);
    }

    public boolean testLimited(MultiblockState blockWorldState) {
        return testGlobal(blockWorldState);
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

    public List<ItemStack> getCandidates() {
        return candidates == null ? Collections.emptyList() : Arrays.stream(this.candidates.get()).filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info->{
            IBlockState blockState = info.getBlockState();
            return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
        }).collect(Collectors.toList());
    }

    public WidgetGroup getConfigWidget() {
        WidgetGroup group = new WidgetGroup(0, 3, 200, 60);
        group.addWidget(new LabelWidget(0, 0, () -> "Type: " + type));
        group.addWidget(new SwitchWidget(0, 15, 40, 15, (cd, r)->{

        }).setBaseTexture(new ColorRectTexture(0xff000000), new TextTexture("unlimited", -1)).setPressedTexture(new ColorRectTexture(0xffff0000), new TextTexture("count", -1)));
        return group;
    }
}
