package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PredicateBlocks extends SimplePredicate {
    public Block[] blocks = new Block[0];
    
    public PredicateBlocks() {
        super("blocks");
    }
    
    public PredicateBlocks(Block... blocks) {
        this();
        this.blocks = blocks;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        predicate = state -> ArrayUtils.contains(blocks, state.getBlockState().getBlock());
        candidates = () -> Arrays.stream(blocks).map(block -> new BlockInfo(block.getDefaultState(), null)).toArray(BlockInfo[]::new);
        return this;
    }

    @Override
    public List<WidgetGroup> getConfigWidget(List<WidgetGroup> groups) {
        super.getConfigWidget(groups);
        WidgetGroup group = new WidgetGroup(0, 0, 200, 100);
        groups.add(group);
        DraggableScrollableWidgetGroup
                container = new DraggableScrollableWidgetGroup(0, 20, 200, 80);
        group.addWidget(container);
        List<IBlockState> blockList = Arrays.stream(blocks).map(Block::getDefaultState).collect(Collectors.toList());
        for (IBlockState blockState : blockList) {
            addBlockSelectorWidget(blockList, container, blockState);
        }
        group.addWidget(new LabelWidget(0, 0, ()->"Blocks").setTextColor(-1).setDrop(true));
        group.addWidget(new ButtonWidget(180, 0, 20, 20, cd -> {
            blockList.add(null);
            addBlockSelectorWidget(blockList, container, null);
        }).setButtonTexture(new ColorRectTexture(-1), new TextTexture("add", 0xff000000)));
        return groups;
    }

    private void addBlockSelectorWidget(List<IBlockState> blockList, DraggableScrollableWidgetGroup container, IBlockState blockState) {
        BlockSelectorWidget bsw = new BlockSelectorWidget(0, container.widgets.size() * 21 + 1, false);
        container.addWidget(bsw);
        bsw.addWidget(new ButtonWidget(181, 1, 18, 18, cd -> {
            int index = (bsw.getSelfPosition().y - 1) / 21;
            blockList.remove(index);
            updateStates(blockList);
            for (int i = index + 1; i < container.widgets.size(); i++) {
                container.widgets.get(i).addSelfPosition(0, -21);
            }
            container.waitToRemoved(bsw);
        }).setButtonTexture(new ColorRectTexture(0xffff0000), new TextTexture("remove", 0xff000000)).setHoverTooltip("remove"));
        if (blockState != null) {
            bsw.setBlock(blockState);
        }
        bsw.setOnBlockStateUpdate(state->{
            int index = (bsw.getSelfPosition().y - 1) / 21;
            blockList.set(index, state);
            updateStates(blockList);
        });
    }

    private void updateStates(List<IBlockState> blockList) {
        blocks = blockList.stream().filter(Objects::nonNull).map(IBlockState::getBlock).toArray(Block[]::new);
        buildPredicate();
    }
}
